package com.jomea.urlshortener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);
    private static final String API_URL = "http://ip-api.com/json/%s?fields=status,country,city,lat,lon,query";
    private static final int CACHE_MAX = 10000;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, GeoLocation> cache;

    public GeoIpService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
        this.mapper = new ObjectMapper();
        this.cache = new ConcurrentHashMap<>();
    }

    public GeoLocation resolve(String ip) {
        if (ip == null || ip.isBlank() || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return null;
        }
        if (cache.size() > CACHE_MAX) cache.clear();
        return cache.computeIfAbsent(ip, this::fetch);
    }

    private GeoLocation fetch(String ip) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_URL, ip)))
                .timeout(Duration.ofSeconds(3))
                .GET().build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;
            JsonNode root = mapper.readTree(res.body());
            if (!"success".equals(root.get("status").asText())) return null;
            return new GeoLocation(
                root.get("country").asText(),
                root.get("city").asText(),
                root.get("lat").asDouble(),
                root.get("lon").asDouble()
            );
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for {}: {}", ip, e.getMessage());
            return null;
        }
    }

    public record GeoLocation(String country, String city, double latitude, double longitude) {}
}
