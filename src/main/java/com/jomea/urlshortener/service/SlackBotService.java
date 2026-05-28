package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AesEncryption;
import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.entity.ClickEvent;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import com.jomea.urlshortener.repository.ClickEventRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SlackBotService {

    private static final Logger log = LoggerFactory.getLogger(SlackBotService.class);

    private final AppSettingsRepository appSettingsRepository;
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlService urlService;
    private final AppProperties appProperties;
    private final AesEncryption aesEncryption;
    private final PasswordEncoder passwordEncoder;

    private App slackApp;
    private SocketModeApp socketModeApp;
    private boolean running;
    private String lastError;

    public SlackBotService(AppSettingsRepository appSettingsRepository,
                           UserRepository userRepository,
                           UrlRepository urlRepository,
                           ClickEventRepository clickEventRepository,
                           UrlService urlService,
                           AppProperties appProperties,
                           AesEncryption aesEncryption,
                           PasswordEncoder passwordEncoder) {
        this.appSettingsRepository = appSettingsRepository;
        this.userRepository = userRepository;
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.urlService = urlService;
        this.appProperties = appProperties;
        this.aesEncryption = aesEncryption;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void autoStart() {
        String botToken = getDecryptedBotToken();
        String appToken = getDecryptedAppToken();
        if (botToken != null && !botToken.isBlank() && appToken != null && !appToken.isBlank()) {
            try {
                startBot(botToken, appToken);
            } catch (Exception e) {
                log.warn("Slack bot auto-start failed: {}", e.getMessage());
                lastError = e.getMessage();
            }
        }
    }

    public synchronized boolean startBot(String botToken, String appToken) {
        try {
            if (socketModeApp != null) stopBot();

            AppConfig config = AppConfig.builder()
                .singleTeamBotToken(botToken)
                .build();
            slackApp = new App(config);

            slackApp.command("/shorten", (req, ctx) -> {
                String text = req.getPayload().getText();
                if (text == null || text.isBlank()) {
                    return ctx.ack("Usage: /shorten <url> [custom_code]");
                }
                String[] parts = text.trim().split("\\s+", 2);
                String longUrl = parts[0];
                String customCode = parts.length > 1 ? parts[1] : null;

                User user = userRepository.findBySlackId(req.getPayload().getUserId()).orElse(null);
                if (user == null) {
                    return ctx.ack("Your Slack is not linked to a Shrtly account. Use `/link` first.");
                }
                try {
                    Url url = urlService.shortenUrlEntity(user.getId(), longUrl, customCode);
                    String shortUrl = appProperties.getBaseUrl() + "/" + url.getShortCode();
                    return ctx.ack("Link shortened! " + shortUrl + " -> " + longUrl);
                } catch (Exception e) {
                    return ctx.ack("Failed to shorten: " + e.getMessage());
                }
            });

            slackApp.command("/link", (req, ctx) -> {
                String text = req.getPayload().getText();
                if (text == null || text.isBlank()) {
                    return ctx.ack("Usage: /link <email> <password>");
                }
                String[] parts = text.trim().split("\\s+", 2);
                if (parts.length < 2) {
                    return ctx.ack("Usage: /link <email> <password>");
                }
                String email = parts[0];
                String password = parts[1];

                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    return ctx.ack("No account found with that email.");
                }
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    return ctx.ack("Invalid password.");
                }
                user.setSlackId(req.getPayload().getUserId());
                user.setSlackTeamId(req.getPayload().getTeamId());
                user.setSlackLinkCode(null);
                userRepository.save(user);
                return ctx.ack("Your Slack account has been linked to **" + user.getName() + "**!");
            });

            slackApp.command("/mylinks", (req, ctx) -> {
                User user = userRepository.findBySlackId(req.getPayload().getUserId()).orElse(null);
                if (user == null) {
                    return ctx.ack("Your Slack is not linked to a Shrtly account. Use `/link` first.");
                }
                String text = req.getPayload().getText();
                int limit = 5;
                if (text != null && !text.isBlank()) {
                    try { limit = Math.min(Integer.parseInt(text.trim()), 25); } catch (NumberFormatException e) {}
                }
                List<Url> urls = urlRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, limit));
                if (urls.isEmpty()) {
                    return ctx.ack("No links found.");
                }
                StringBuilder sb = new StringBuilder("*Your recent links:*\n");
                for (Url u : urls) {
                    sb.append("\u2022 `").append(u.getShortCode()).append("` \u2192 ")
                      .append(truncate(u.getLongUrl(), 60)).append("\n");
                }
                return ctx.ack(sb.toString());
            });

            slackApp.command("/analytics", (req, ctx) -> {
                String code = req.getPayload().getText();
                if (code == null || code.isBlank()) {
                    return ctx.ack("Usage: /analytics <short_code>");
                }
                User user = userRepository.findBySlackId(req.getPayload().getUserId()).orElse(null);
                if (user == null) {
                    return ctx.ack("Your Slack is not linked to a Shrtly account. Use `/link` first.");
                }
                code = code.trim().split("\\s+")[0];
                Url url = urlRepository.findByShortCode(code).orElse(null);
                if (url == null) {
                    return ctx.ack("No link found with code `" + code + "`.");
                }
                if (url.getUserId() == null || !url.getUserId().equals(user.getId())) {
                    return ctx.ack("That link does not belong to you.");
                }
                List<ClickEvent> clicks = clickEventRepository.findByShortCodeOrderByTimestampDesc(code);
                if (clicks.isEmpty()) {
                    return ctx.ack("No analytics yet for `" + code + "`.");
                }
                Map<String, Long> browsers = clicks.stream()
                    .filter(e -> e.getUserAgent() != null)
                    .collect(Collectors.groupingBy(e -> classifyBrowser(e.getUserAgent()), Collectors.counting()));
                Map<String, Long> countries = clicks.stream()
                    .filter(e -> e.getCountry() != null)
                    .collect(Collectors.groupingBy(ClickEvent::getCountry, Collectors.counting()));
                String topBrowser = browsers.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
                String topCountry = countries.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
                String msg = "*Analytics for `" + code + "`*\n"
                    + "\u2022 Total clicks: " + clicks.size() + "\n"
                    + "\u2022 Top browser: " + topBrowser + "\n"
                    + "\u2022 Top country: " + topCountry + "\n"
                    + "\u2022 Latest: " + (clicks.isEmpty() ? "N/A" : clicks.get(0).getTimestamp().toString());
                return ctx.ack(msg);
            });

            socketModeApp = new SocketModeApp(appToken, slackApp);
            socketModeApp.start();
            running = true;
            lastError = null;
            log.info("Slack bot started successfully");
            return true;
        } catch (Exception e) {
            running = false;
            lastError = e.getMessage();
            log.error("Slack bot failed to start: {}", e.getMessage());
            return false;
        }
    }

    public synchronized void stopBot() {
        if (socketModeApp != null) {
            try {
                socketModeApp.stop();
            } catch (Exception e) {
                log.warn("Error disconnecting Slack bot: {}", e.getMessage());
            }
            socketModeApp = null;
        }
        slackApp = null;
        running = false;
        log.info("Slack bot stopped");
    }

    public boolean isRunning() { return running; }
    public String getLastError() { return lastError; }

    public String getDecryptedBotToken() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null || settings.getSlackBotToken() == null) return null;
        return aesEncryption.decrypt(settings.getSlackBotToken());
    }

    public String getDecryptedAppToken() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null || settings.getSlackAppToken() == null) return null;
        return aesEncryption.decrypt(settings.getSlackAppToken());
    }

    private String classifyBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("edge") || ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome") && !ua.contains("chromium")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("opera") || ua.contains("opr/")) return "Opera";
        return "Other";
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    @PreDestroy
    public void shutdown() {
        stopBot();
    }
}
