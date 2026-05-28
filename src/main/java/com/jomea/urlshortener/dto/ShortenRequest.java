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
        String utmContent,
        Long folderId,
        Long workspaceId,
        String ogTitle,
        String ogDescription,
        String ogImage
) {
    public ShortenRequest {
        if (tags == null) tags = "";
        if (utmSource == null) utmSource = "";
        if (utmMedium == null) utmMedium = "";
        if (utmCampaign == null) utmCampaign = "";
        if (utmTerm == null) utmTerm = "";
        if (utmContent == null) utmContent = "";
        if (ogTitle == null) ogTitle = "";
        if (ogDescription == null) ogDescription = "";
        if (ogImage == null) ogImage = "";
    }
}
