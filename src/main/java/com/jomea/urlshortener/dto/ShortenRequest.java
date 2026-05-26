package com.jomea.urlshortener.dto;

import org.hibernate.validator.constraints.URL;

public record ShortenRequest(
        @URL String url
) {
}
