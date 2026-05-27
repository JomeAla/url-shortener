package com.jomea.urlshortener.dto;

public record BulkShortenResponseItem(
    int index,
    String status,
    String shortCode,
    String error,
    String shortUrl,
    String longUrl
) {}
