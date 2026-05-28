package com.jomea.urlshortener.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanDto(
    Long id,
    String name,
    String slug,
    String description,
    BigDecimal price,
    String currency,
    String billingPeriod,
    int maxUrls,
    int maxClicksPerUrl,
    boolean customDomains,
    boolean apiAccess,
    boolean hasQrCodes,
    boolean hasCustomCodes,
    boolean hasBulkImport,
    boolean hasAdvancedAnalytics,
    boolean hasWebhooks,
    boolean hasTeamAccess,
    int maxRequestsPerMinute,
    String features,
    int sortOrder,
    boolean active,
    LocalDateTime createdAt
) {}
