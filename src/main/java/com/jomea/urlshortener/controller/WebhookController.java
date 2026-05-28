package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.entity.Webhook;
import com.jomea.urlshortener.entity.WebhookLog;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.WebhookService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final WebhookService webhookService;
    private final UserRepository userRepository;

    public WebhookController(WebhookService webhookService, UserRepository userRepository) {
        this.webhookService = webhookService;
        this.userRepository = userRepository;
    }

    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        Optional<User> user = userRepository.findByEmail(auth.getName());
        return user.map(User::getId).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String url = (String) body.get("url");
        String eventsRaw = (String) body.get("events");
        String secret = (String) body.get("secret");

        if (url == null || url.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        if (eventsRaw == null || eventsRaw.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "At least one event is required"));

        List<String> events = Arrays.asList(eventsRaw.split(","));
        Webhook webhook = webhookService.createWebhook(userId, url, events, secret);
        return ResponseEntity.ok(Map.of(
            "id", webhook.getId(),
            "url", webhook.getUrl(),
            "events", webhook.getEvents(),
            "active", webhook.isActive(),
            "createdAt", webhook.getCreatedAt().toString()
        ));
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        List<Webhook> webhooks = webhookService.getUserWebhooks(userId);
        List<Map<String, Object>> result = webhooks.stream().map(w -> Map.<String, Object>of(
            "id", w.getId(),
            "url", w.getUrl(),
            "events", w.getEvents(),
            "active", w.isActive(),
            "createdAt", w.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        return webhookService.getWebhook(id, userId)
            .map(w -> ResponseEntity.ok(Map.<String, Object>of(
                "id", w.getId(),
                "url", w.getUrl(),
                "events", w.getEvents(),
                "secret", w.getSecret() != null ? "********" : null,
                "active", w.isActive(),
                "createdAt", w.getCreatedAt().toString()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        String url = (String) body.get("url");
        String eventsRaw = (String) body.get("events");
        String secret = (String) body.get("secret");
        Boolean active = body.containsKey("active") ? (Boolean) body.get("active") : null;

        try {
            List<String> events = eventsRaw != null ? Arrays.asList(eventsRaw.split(",")) : null;
            Webhook webhook = webhookService.updateWebhook(id, userId, url, events, secret, active);
            return ResponseEntity.ok(Map.<String, Object>of(
                "id", webhook.getId(),
                "url", webhook.getUrl(),
                "events", webhook.getEvents(),
                "active", webhook.isActive(),
                "createdAt", webhook.getCreatedAt().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        try {
            webhookService.deleteWebhook(id, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<?> test(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        Optional<Webhook> opt = webhookService.getWebhook(id, userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Webhook webhook = opt.get();
        String testPayload = """
            {"event":"test","timestamp":"%s","data":{"message":"This is a test webhook from Shrtly"}}
            """.formatted(LocalDateTime.now().toString());

        webhookService.dispatch(webhook, "test", testPayload);
        return ResponseEntity.ok(Map.of("success", true, "message", "Test webhook dispatched"));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<?> logs(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        try {
            List<WebhookLog> logs = webhookService.getWebhookLogs(id, userId);
            List<Map<String, Object>> result = logs.stream().map(l -> Map.<String, Object>of(
                "id", l.getId(),
                "event", l.getEvent(),
                "success", l.isSuccess(),
                "responseCode", l.getResponseCode(),
                "errorMessage", l.getErrorMessage(),
                "createdAt", l.getCreatedAt().toString()
            )).toList();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/logs/{logId}")
    public ResponseEntity<?> logDetail(@PathVariable Long id, @PathVariable Long logId, Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));

        try {
            List<WebhookLog> logs = webhookService.getWebhookLogs(id, userId);
            return logs.stream()
                .filter(l -> l.getId().equals(logId))
                .findFirst()
                .map(l -> ResponseEntity.ok(Map.<String, Object>of(
                    "id", l.getId(),
                    "webhookId", l.getWebhookId(),
                    "event", l.getEvent(),
                    "payload", l.getPayload(),
                    "responseCode", l.getResponseCode(),
                    "responseBody", l.getResponseBody(),
                    "success", l.isSuccess(),
                    "errorMessage", l.getErrorMessage(),
                    "createdAt", l.getCreatedAt().toString()
                )))
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
