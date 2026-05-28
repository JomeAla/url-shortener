package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.ClickEvent;
import com.jomea.urlshortener.entity.CustomDomain;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.entity.Webhook;
import com.jomea.urlshortener.repository.ClickEventRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.repository.WebhookRepository;
import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.service.CustomDomainService;
import com.jomea.urlshortener.service.TierEnforcementService;
import com.jomea.urlshortener.service.UrlService;
import com.jomea.urlshortener.service.WebhookService;
import com.jomea.urlshortener.service.GeoIpService;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.view.RedirectView;

@RestController
public class RedirectController {

    private final UrlService urlService;
    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final TierEnforcementService tierEnforcement;
    private final UserRepository userRepository;
    private final WebhookService webhookService;
    private final WebhookRepository webhookRepository;
    private final GeoIpService geoIpService;
    private final AppProperties appProperties;
    private final CustomDomainService customDomainService;

    public RedirectController(UrlService urlService, UrlRepository urlRepository,
                               ClickEventRepository clickEventRepository,
                               TierEnforcementService tierEnforcement,
                               UserRepository userRepository,
                               WebhookService webhookService,
                               WebhookRepository webhookRepository,
                               GeoIpService geoIpService,
                               AppProperties appProperties,
                               CustomDomainService customDomainService) {
        this.urlService = urlService;
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.tierEnforcement = tierEnforcement;
        this.userRepository = userRepository;
        this.webhookService = webhookService;
        this.webhookRepository = webhookRepository;
        this.geoIpService = geoIpService;
        this.appProperties = appProperties;
        this.customDomainService = customDomainService;
    }

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/index.html");
    }

    @GetMapping("/{shortCode:[0-9A-Za-z]{4,20}}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        var details = urlService.getShortCodeDetails(shortCode);
        if (details.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Url url = details.get();

        String host = request.getServerName();
        String mainHost = appProperties.getBaseUrl().replace("http://", "").replace("https://", "").split(":")[0].split("/")[0];
        if (!host.equals("localhost") && !host.equals("127.0.0.1") && !host.equals(mainHost)) {
            var domainOpt = customDomainService.findVerifiedDomain(host);
            if (domainOpt.isPresent()) {
                CustomDomain cd = domainOpt.get();
                if (url.getUserId() == null || !url.getUserId().equals(cd.getUserId())) {
                    return ResponseEntity.notFound().build();
                }
            }
        }
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "This link has expired"));
        }
        if (url.getPasswordHash() != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("requiresPassword", true, "shortCode", shortCode));
        }
        if (url.getUserId() != null) {
            User owner = userRepository.findById(url.getUserId()).orElse(null);
            if (owner != null) {
                try {
                    tierEnforcement.checkCanAccessUrl(owner, url.getClickCount());
                } catch (IllegalStateException e) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", e.getMessage()));
                }
            }
        }
        String referer = request.getHeader("Referer");
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        String finalShortCode = shortCode;
        Url finalUrl = url;
        Thread.ofVirtual().start(() -> {
            urlRepository.incrementClickCount(finalShortCode);
            ClickEvent event = new ClickEvent();
            event.setShortCode(finalShortCode);
            event.setTimestamp(java.time.LocalDateTime.now());
            event.setReferer(referer);
            event.setUserAgent(userAgent);
            event.setIpAddress(ipAddress);
            if (ipAddress != null) {
                var geo = geoIpService.resolve(ipAddress);
                if (geo != null) {
                    event.setCountry(geo.country());
                    event.setCity(geo.city());
                    event.setLatitude(geo.latitude());
                    event.setLongitude(geo.longitude());
                }
            }
            clickEventRepository.save(event);

            if (finalUrl.getUserId() != null) {
                List<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(finalUrl.getUserId());
                String clickPayload = """
                    {"event":"link.clicked","timestamp":"%s","data":{"shortCode":"%s","longUrl":"%s","referer":"%s","userAgent":"%s","ipAddress":"%s"}}
                    """.formatted(LocalDateTime.now().toString(), finalShortCode,
                        finalUrl.getLongUrl().replace("\"", "\\\""),
                        referer != null ? referer.replace("\"", "\\\"") : "",
                        userAgent != null ? userAgent.replace("\"", "\\\"") : "",
                        ipAddress != null ? ipAddress : "");
                for (Webhook w : webhooks) {
                    if (w.getEvents().contains("link.clicked") || w.getEvents().contains("*")) {
                        webhookService.dispatch(w, "link.clicked", clickPayload);
                    }
                }
            }
        });
        String redirectUrl = url.getLongUrl();
        Map<String, String> utmParams = new LinkedHashMap<>();
        if (url.getUtmSource() != null && !url.getUtmSource().isBlank()) utmParams.put("utm_source", url.getUtmSource());
        if (url.getUtmMedium() != null && !url.getUtmMedium().isBlank()) utmParams.put("utm_medium", url.getUtmMedium());
        if (url.getUtmCampaign() != null && !url.getUtmCampaign().isBlank()) utmParams.put("utm_campaign", url.getUtmCampaign());
        if (url.getUtmTerm() != null && !url.getUtmTerm().isBlank()) utmParams.put("utm_term", url.getUtmTerm());
        if (url.getUtmContent() != null && !url.getUtmContent().isBlank()) utmParams.put("utm_content", url.getUtmContent());
        if (!utmParams.isEmpty()) {
            String separator = redirectUrl.contains("?") ? "&" : "?";
            redirectUrl += separator + utmParams.entrySet().stream()
                .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        }
        if (userAgent != null && isSocialCrawler(userAgent) && (url.getOgTitle() != null || url.getOgDescription() != null)) {
            return serveOgPage(url, redirectUrl);
        }
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }

    private static final Set<String> CRAWLERS = Set.of(
        "twitterbot", "facebookexternalhit", "slackbot", "linkedinbot",
        "whatsapp", "telegrambot", "discordbot", "googlebot",
        "pinterest", "redditbot", "skypeuripreview"
    );

    private boolean isSocialCrawler(String userAgent) {
        String ua = userAgent.toLowerCase();
        return CRAWLERS.stream().anyMatch(ua::contains);
    }

    private ResponseEntity<String> serveOgPage(Url url, String redirectUrl) {
        String title = url.getOgTitle() != null ? escapeHtml(url.getOgTitle()) : "Shrtly Link";
        String description = url.getOgDescription() != null ? escapeHtml(url.getOgDescription()) : "";
        String image = url.getOgImage() != null ? escapeHtml(url.getOgImage()) : "";
        String destUrl = escapeHtml(redirectUrl);
        String shortUrl = escapeHtml(appProperties.getBaseUrl() + "/" + url.getShortCode());

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8">
            <title>%s</title>
            <meta property="og:title" content="%s" />
            <meta property="og:description" content="%s" />
            <meta property="og:url" content="%s" />
            <meta name="twitter:card" content="summary_large_image" />
            <meta name="twitter:title" content="%s" />
            <meta name="twitter:description" content="%s" />
            <meta http-equiv="refresh" content="0; url=%s" />
            </head>
            <body>
            <script>window.location.href="%s";</script>
            </body>
            </html>
            """.formatted(title, title, description, shortUrl, title, description, destUrl, destUrl);

        if (!image.isBlank()) {
            html = html.replace("</head>",
                "<meta property=\"og:image\" content=\"" + image + "\" />\n<meta name=\"twitter:image\" content=\"" + image + "\" />\n</head>");
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
            .body(html);
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
