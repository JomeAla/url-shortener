package com.jomea.urlshortener.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
        @URL String url,
        @Size(min = 4, max = 20) String customCode,
        String expiresAt,
        String password
) {
}
