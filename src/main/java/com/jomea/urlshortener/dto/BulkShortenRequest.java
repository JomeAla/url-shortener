package com.jomea.urlshortener.dto;

public record BulkShortenRequest(
    String url,
    String customCode,
    String expiresAt,
    String password
) {}
