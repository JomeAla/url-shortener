package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.AnalyticsResponse;
import com.jomea.urlshortener.dto.AnalyticsResponse.RecentClick;
import com.jomea.urlshortener.repository.ClickEventRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jomea.urlshortener.entity.ClickEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final ClickEventRepository clickEventRepository;
    private final UrlRepository urlRepository;

    public AnalyticsController(ClickEventRepository clickEventRepository, UrlRepository urlRepository) {
        this.clickEventRepository = clickEventRepository;
        this.urlRepository = urlRepository;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        if (urlRepository.findByShortCode(shortCode).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ClickEvent> events =
            clickEventRepository.findByShortCodeOrderByTimestampDesc(shortCode);

        Map<String, Long> referrers = events.stream()
            .filter(e -> e.getReferer() != null && !e.getReferer().isBlank())
            .collect(Collectors.groupingBy(
                e -> extractDomain(e.getReferer()),
                Collectors.counting()
            ));

        Map<String, Long> browsers = events.stream()
            .filter(e -> e.getUserAgent() != null && !e.getUserAgent().isBlank())
            .collect(Collectors.groupingBy(
                e -> classifyBrowser(e.getUserAgent()),
                Collectors.counting()
            ));

        Map<String, Long> devices = events.stream()
            .filter(e -> e.getUserAgent() != null && !e.getUserAgent().isBlank())
            .collect(Collectors.groupingBy(
                e -> classifyDevice(e.getUserAgent()),
                Collectors.counting()
            ));

        List<RecentClick> recent = events.stream()
            .limit(20)
            .map(e -> new RecentClick(
                e.getTimestamp(), e.getReferer(), e.getUserAgent(), e.getIpAddress()
            ))
            .toList();

        return ResponseEntity.ok(new AnalyticsResponse(
            shortCode,
            (long) events.size(),
            referrers,
            browsers,
            devices,
            recent
        ));
    }

    private String extractDomain(String referer) {
        try {
            String domain = referer.replaceFirst("^https?://", "").replaceFirst("/.*$", "");
            return domain.isEmpty() ? "direct" : domain;
        } catch (Exception e) {
            return "unknown";
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

    private String classifyDevice(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") && ua.contains("mobile")) return "Mobile";
        if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet";
        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) return "Bot";
        return "Desktop";
    }
}
