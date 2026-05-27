package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.AnalyticsResponse;
import com.jomea.urlshortener.dto.AnalyticsResponse.RecentClick;
import com.jomea.urlshortener.dto.TimeSeriesResponse;
import com.jomea.urlshortener.dto.TimeSeriesResponse.DataPoint;
import com.jomea.urlshortener.entity.ClickEvent;
import com.jomea.urlshortener.repository.ClickEventRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{shortCode}/timeseries")
    public ResponseEntity<?> getTimeSeries(@PathVariable String shortCode,
                                            @RequestParam(defaultValue = "day") String interval,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        if (urlRepository.findByShortCode(shortCode).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        LocalDateTime toDate = to != null ? LocalDateTime.parse(to) : LocalDateTime.now();
        LocalDateTime fromDate = from != null ? LocalDateTime.parse(from) : toDate.minusDays(7);

        List<ClickEvent> events = clickEventRepository
            .findByShortCodeAndTimestampBetweenOrderByTimestampAsc(shortCode, fromDate, toDate);

        List<DataPoint> dataPoints;
        if ("hour".equals(interval)) {
            Map<LocalDateTime, Long> grouped = new LinkedHashMap<>();
            LocalDateTime current = fromDate.withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = toDate.withMinute(0).withSecond(0).withNano(0);
            while (!current.isAfter(end)) {
                grouped.put(current, 0L);
                current = current.plusHours(1);
            }
            for (ClickEvent e : events) {
                LocalDateTime bucket = e.getTimestamp().withMinute(0).withSecond(0).withNano(0);
                grouped.merge(bucket, 1L, Long::sum);
            }
            dataPoints = grouped.entrySet().stream()
                .map(e -> new DataPoint(e.getKey().toString(), e.getValue()))
                .toList();
        } else {
            Map<LocalDateTime, Long> grouped = new LinkedHashMap<>();
            LocalDateTime current = fromDate.toLocalDate().atStartOfDay();
            LocalDateTime end = toDate.toLocalDate().atStartOfDay();
            while (!current.isAfter(end)) {
                grouped.put(current, 0L);
                current = current.plusDays(1);
            }
            for (ClickEvent e : events) {
                LocalDateTime bucket = e.getTimestamp().toLocalDate().atStartOfDay();
                grouped.merge(bucket, 1L, Long::sum);
            }
            dataPoints = grouped.entrySet().stream()
                .map(e -> new DataPoint(e.getKey().toLocalDate().toString(), e.getValue()))
                .toList();
        }

        return ResponseEntity.ok(new TimeSeriesResponse(interval, dataPoints));
    }

    @GetMapping("/{shortCode}/export/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable String shortCode) {
        if (urlRepository.findByShortCode(shortCode).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ClickEvent> events = clickEventRepository.findByShortCodeOrderByTimestampDesc(shortCode);

        StringBuilder sb = new StringBuilder();
        sb.append("shortCode,timestamp,referer,userAgent,ipAddress\n");
        for (ClickEvent e : events) {
            sb.append(escapeCsv(e.getShortCode())).append(",");
            sb.append(e.getTimestamp()).append(",");
            sb.append(escapeCsv(e.getReferer())).append(",");
            sb.append(escapeCsv(e.getUserAgent())).append(",");
            sb.append(escapeCsv(e.getIpAddress())).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", shortCode + "-clicks.csv");

        return ResponseEntity.ok()
            .headers(headers)
            .body(sb.toString().getBytes());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
