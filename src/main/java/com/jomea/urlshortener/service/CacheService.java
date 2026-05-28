package com.jomea.urlshortener.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class CacheService {

    private final StringRedisTemplate redis;
    private final Map<String, String> localCache = new ConcurrentHashMap<>();

    private static final long DEFAULT_TTL_HOURS = 24;

    public CacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void put(String shortCode, String longUrl) {
        localCache.put(shortCode, longUrl);
        redis.opsForValue().set(shortCode, longUrl, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
    }

    public Optional<String> get(String shortCode) {
        String cached = localCache.get(shortCode);
        if (cached != null) return Optional.of(cached);

        String fromRedis = redis.opsForValue().get(shortCode);
        if (fromRedis != null) {
            localCache.put(shortCode, fromRedis);
            return Optional.of(fromRedis);
        }

        return Optional.empty();
    }

    public void invalidate(String shortCode) {
        localCache.remove(shortCode);
        redis.delete(shortCode);
    }
}