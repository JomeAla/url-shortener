package com.jomea.urlshortener.dto;

public record ShortenResponse(
        String shortUrl,
        String shortCode,
        String longUrl,
        String expiresAt,
        boolean hasPassword,
        boolean isCustom,
        String tags,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        String utmTerm,
        String utmContent
) {
}
