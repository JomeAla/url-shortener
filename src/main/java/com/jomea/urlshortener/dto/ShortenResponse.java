package com.jomea.urlshortener.dto;

public record ShortenResponse(
        String shortUrl,
        String shortCode,
        String longUrl,
        String expiresAt,
        boolean hasPassword,
        boolean isCustom
) {
}
