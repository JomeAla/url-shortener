package com.jomea.urlshortener.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public void put(String shortCode, String longUrl) {
        cache.put(shortCode, longUrl);
    }

    public Optional<String> get(String shortCode) {
        return Optional.ofNullable(cache.get(shortCode));
    }

    public void invalidate(String shortCode) {
        cache.remove(shortCode);
    }
}
