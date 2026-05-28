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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscordBotService {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotService.class);

    private JDA jda;
    private boolean running;
    private String lastError;

    private final AppSettingsRepository appSettingsRepository;
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlService urlService;
    private final AesEncryption aesEncryption;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;

    public DiscordBotService(AppSettingsRepository appSettingsRepository,
                              UserRepository userRepository,
                              UrlRepository urlRepository,
                              ClickEventRepository clickEventRepository,
                              UrlService urlService,
                              AesEncryption aesEncryption,
                              AppProperties appProperties,
                              PasswordEncoder passwordEncoder) {
        this.appSettingsRepository = appSettingsRepository;
        this.userRepository = userRepository;
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.urlService = urlService;
        this.aesEncryption = aesEncryption;
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void autoStart() {
        String token = getDecryptedToken();
        if (token != null && !token.isBlank()) {
            try {
                startBot(token);
            } catch (Exception e) {
                log.warn("Discord bot auto-start failed: {}", e.getMessage());
                lastError = e.getMessage();
            }
        }
    }

    public synchronized boolean startBot(String token) {
        try {
            if (jda != null) stopBot();
            jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordListener())
                .build()
                .awaitReady();
            jda.updateCommands().addCommands(
                Commands.slash("link", "Link your Discord account to Shrtly")
                    .addOption(OptionType.STRING, "email", "Your Shrtly account email", true)
                    .addOption(OptionType.STRING, "password", "Your Shrtly account password", true),
                Commands.slash("shorten", "Shorten a URL")
                    .addOption(OptionType.STRING, "url", "The long URL to shorten", true)
                    .addOption(OptionType.STRING, "custom", "Optional custom short code", false),
                Commands.slash("mylinks", "List your recent short links")
                    .addOption(OptionType.INTEGER, "limit", "Number of links to show (default 5)", false),
                Commands.slash("analytics", "View analytics for a link")
                    .addOption(OptionType.STRING, "code", "The short code", true)
            ).queue();
            running = true;
            lastError = null;
            log.info("Discord bot started successfully");
            return true;
        } catch (Exception e) {
            running = false;
            lastError = e.getMessage();
            log.error("Discord bot failed to start: {}", e.getMessage());
            return false;
        }
    }

    public synchronized void stopBot() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        running = false;
        log.info("Discord bot stopped");
    }

    public boolean isRunning() { return running; }
    public String getLastError() { return lastError; }

    public String getDecryptedToken() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null || settings.getDiscordBotToken() == null) return null;
        return aesEncryption.decrypt(settings.getDiscordBotToken());
    }

    @PreDestroy
    public void shutdown() {
        stopBot();
    }

    private class DiscordListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            try {
                switch (event.getName()) {
                    case "link" -> handleLink(event);
                    case "shorten" -> handleShorten(event);
                    case "mylinks" -> handleMyLinks(event);
                    case "analytics" -> handleAnalytics(event);
                }
            } catch (Exception e) {
                event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
            }
        }

        private void handleLink(SlashCommandInteractionEvent event) {
            String email = event.getOption("email").getAsString();
            String password = event.getOption("password").getAsString();
            event.deferReply(true).queue(hook -> {
                try {
                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user == null) {
                        hook.editOriginal("No account found with that email.").queue();
                        return;
                    }
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        hook.editOriginal("Invalid password.").queue();
                        return;
                    }
                    user.setDiscordId(event.getUser().getId());
                    user.setDiscordLinkCode(null);
                    userRepository.save(user);
                    hook.editOriginal("Your Discord account has been linked to **" + user.getName() + "**! You can now use `/shorten`, `/mylinks`, and `/analytics`.").queue();
                } catch (Exception e) {
                    hook.editOriginal("Failed to link: " + e.getMessage()).queue();
                }
            });
        }

        private void handleShorten(SlashCommandInteractionEvent event) {
            event.deferReply(true).queue(hook -> {
                User user = userRepository.findByDiscordId(event.getUser().getId()).orElse(null);
                if (user == null) {
                    hook.editOriginal("Your Discord is not linked to a Shrtly account. Use `/link` first.").queue();
                    return;
                }
                String longUrl = event.getOption("url").getAsString();
                String customCode = event.getOption("custom") != null ? event.getOption("custom").getAsString() : null;
                try {
                    Url url = urlService.shortenUrlEntity(user.getId(), longUrl, customCode);
                    String shortUrl = appProperties.getBaseUrl() + "/" + url.getShortCode();
                    hook.editOriginal("Link shortened! **" + shortUrl + "**\n\u2192 " + longUrl).queue();
                } catch (Exception e) {
                    hook.editOriginal("Failed to shorten: " + e.getMessage()).queue();
                }
            });
        }

        private void handleMyLinks(SlashCommandInteractionEvent event) {
            event.deferReply(true).queue(hook -> {
                User user = userRepository.findByDiscordId(event.getUser().getId()).orElse(null);
                if (user == null) {
                    hook.editOriginal("Your Discord is not linked to a Shrtly account. Use `/link` first.").queue();
                    return;
                }
                int limit = event.getOption("limit") != null ? Math.min(event.getOption("limit").getAsInt(), 25) : 5;
                List<Url> urls = urlRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), org.springframework.data.domain.PageRequest.of(0, limit));
                if (urls.isEmpty()) {
                    hook.editOriginal("No links found.").queue();
                    return;
                }
                StringBuilder sb = new StringBuilder("**Your recent links:**\n");
                for (Url u : urls) {
                    sb.append("\u2022 `").append(u.getShortCode()).append("` \u2192 ")
                      .append(truncate(u.getLongUrl(), 60)).append("\n");
                }
                hook.editOriginal(sb.toString()).queue();
            });
        }

        private void handleAnalytics(SlashCommandInteractionEvent event) {
            event.deferReply(true).queue(hook -> {
                User user = userRepository.findByDiscordId(event.getUser().getId()).orElse(null);
                if (user == null) {
                    hook.editOriginal("Your Discord is not linked to a Shrtly account. Use `/link` first.").queue();
                    return;
                }
                String code = event.getOption("code").getAsString();
                Url url = urlRepository.findByShortCode(code).orElse(null);
                if (url == null) {
                    hook.editOriginal("No link found with code `" + code + "`.").queue();
                    return;
                }
                if (url.getUserId() == null || !url.getUserId().equals(user.getId())) {
                    hook.editOriginal("That link does not belong to you.").queue();
                    return;
                }
                List<ClickEvent> clicks = clickEventRepository.findByShortCodeOrderByTimestampDesc(code);
                if (clicks.isEmpty()) {
                    hook.editOriginal("No analytics yet for `" + code + "`.").queue();
                    return;
                }
                Map<String, Long> browsers = clicks.stream()
                    .filter(e -> e.getUserAgent() != null)
                    .collect(Collectors.groupingBy(e -> classifyBrowser(e.getUserAgent()), Collectors.counting()));
                Map<String, Long> countries = clicks.stream()
                    .filter(e -> e.getCountry() != null)
                    .collect(Collectors.groupingBy(ClickEvent::getCountry, Collectors.counting()));
                String topBrowser = browsers.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
                String topCountry = countries.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("N/A");
                String msg = "**Analytics for `" + code + "`**\n"
                    + "\u2022 Total clicks: " + clicks.size() + "\n"
                    + "\u2022 Top browser: " + topBrowser + "\n"
                    + "\u2022 Top country: " + topCountry + "\n"
                    + "\u2022 Latest: " + (clicks.isEmpty() ? "N/A" : clicks.get(0).getTimestamp().toString());
                hook.editOriginal(msg).queue();
            });
        }
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
}
