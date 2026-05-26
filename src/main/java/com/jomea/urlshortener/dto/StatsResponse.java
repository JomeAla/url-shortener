package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;

public record StatsResponse(
        String shortCode,
        String longUrl,
        long clickCount,
        LocalDateTime createdAt
) {
}
