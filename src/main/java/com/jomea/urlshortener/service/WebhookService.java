package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.Webhook;
import com.jomea.urlshortener.entity.WebhookLog;
import com.jomea.urlshortener.repository.WebhookLogRepository;
import com.jomea.urlshortener.repository.WebhookRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final HttpClient httpClient;

    public WebhookService(WebhookRepository webhookRepository, WebhookLogRepository webhookLogRepository) {
        this.webhookRepository = webhookRepository;
        this.webhookLogRepository = webhookLogRepository;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public Webhook createWebhook(Long userId, String url, List<String> events, String secret) {
        Webhook webhook = new Webhook();
        webhook.setUserId(userId);
        webhook.setUrl(url);
        webhook.setEvents(String.join(",", events));
        webhook.setSecret(secret);
        webhook.setActive(true);
        webhook.setCreatedAt(LocalDateTime.now());
        return webhookRepository.save(webhook);
    }

    public List<Webhook> getUserWebhooks(Long userId) {
        return webhookRepository.findByUserId(userId);
    }

    public Optional<Webhook> getWebhook(Long id, Long userId) {
        return webhookRepository.findByIdAndUserId(id, userId);
    }

    public Webhook updateWebhook(Long id, Long userId, String url, List<String> events, String secret, Boolean active) {
        Webhook webhook = webhookRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        if (url != null) webhook.setUrl(url);
        if (events != null) webhook.setEvents(String.join(",", events));
        if (secret != null) webhook.setSecret(secret.isEmpty() ? null : secret);
        if (active != null) webhook.setActive(active);
        return webhookRepository.save(webhook);
    }

    public void deleteWebhook(Long id, Long userId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        webhookRepository.delete(webhook);
    }

    public List<WebhookLog> getWebhookLogs(Long webhookId, Long userId) {
        Webhook webhook = webhookRepository.findByIdAndUserId(webhookId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        return webhookLogRepository.findByWebhookIdOrderByCreatedAtDesc(webhook.getId());
    }

    @Async
    public void dispatch(Webhook webhook, String event, String payload) {
        if (!webhook.isActive()) return;
        doDispatchWithRetry(webhook, event, payload, 0);
    }

    private void doDispatchWithRetry(Webhook webhook, String event, String payload, int attempt) {
        WebhookLog whLog = new WebhookLog();
        whLog.setWebhookId(webhook.getId());
        whLog.setEvent(event);
        whLog.setPayload(payload);
        whLog.setCreatedAt(LocalDateTime.now());

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(webhook.getUrl()))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("X-Webhook-Event", event);

            if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
                String signature = signPayload(payload, webhook.getSecret());
                builder.header("X-Webhook-Signature", "sha256=" + signature);
            }

            HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            whLog.setResponseCode(response.statusCode());
            whLog.setResponseBody(response.body().length() > 2048 ? response.body().substring(0, 2048) : response.body());
            whLog.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);

            if (!whLog.isSuccess() && attempt < MAX_RETRIES - 1) {
                whLog.setErrorMessage("Retrying after attempt " + (attempt + 1));
                webhookLogRepository.save(whLog);
                long backoff = (long) Math.pow(2, attempt) * 1000;
                try { Thread.sleep(backoff); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                doDispatchWithRetry(webhook, event, payload, attempt + 1);
                return;
            }
        } catch (Exception e) {
            whLog.setSuccess(false);
            whLog.setErrorMessage(e.getMessage() != null ? e.getMessage().substring(0, Math.min(1024, e.getMessage().length())) : "Unknown error");

            if (attempt < MAX_RETRIES - 1) {
                whLog.setErrorMessage("Retrying after attempt " + (attempt + 1) + ": " + whLog.getErrorMessage());
                webhookLogRepository.save(whLog);
                long backoff = (long) Math.pow(2, attempt) * 1000;
                try { Thread.sleep(backoff); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                doDispatchWithRetry(webhook, event, payload, attempt + 1);
                return;
            }
        }

        webhookLogRepository.save(whLog);
    }

    private String signPayload(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            log.warn("Failed to sign webhook payload", e);
            return "";
        }
    }
}
