package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
        String shortCode,
        long totalClicks,
        Map<String, Long> referrers,
        Map<String, Long> browsers,
        Map<String, Long> devices,
        List<RecentClick> recentClicks
) {
    public record RecentClick(
            LocalDateTime timestamp,
            String referer,
            String userAgent,
            String ipAddress
    ) {}
}
