package com.jomea.urlshortener.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
        @URL String url,
        @Size(min = 4, max = 20) String customCode,
        String expiresAt,
        String password,
        String tags,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        String utmTerm,
        String utmContent
) {
    public ShortenRequest {
        if (tags == null) tags = "";
        if (utmSource == null) utmSource = "";
        if (utmMedium == null) utmMedium = "";
        if (utmCampaign == null) utmCampaign = "";
        if (utmTerm == null) utmTerm = "";
        if (utmContent == null) utmContent = "";
    }
}
