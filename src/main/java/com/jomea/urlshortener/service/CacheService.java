package com.jomea.urlshortener.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheService {

    private static final String KEY_PREFIX = "url:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public CacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String shortCode, String longUrl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + shortCode, longUrl, TTL);
    }

    public Optional<String> get(String shortCode) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + shortCode);
        return Optional.ofNullable(value);
    }

    public void invalidate(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}
