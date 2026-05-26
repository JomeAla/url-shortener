package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.dto.ShortenResponse;
import com.jomea.urlshortener.dto.StatsResponse;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.UrlRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final IdGenerator idGenerator;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    public UrlService(UrlRepository urlRepository, IdGenerator idGenerator,
                      CacheService cacheService, AppProperties appProperties) {
        this.urlRepository = urlRepository;
        this.idGenerator = idGenerator;
        this.cacheService = cacheService;
        this.appProperties = appProperties;
    }

    public ShortenResponse shortenUrl(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }
        if (longUrl.length() > appProperties.getMaxUrlLength()) {
            throw new IllegalArgumentException(
                    "URL exceeds maximum length of " + appProperties.getMaxUrlLength());
        }
        try {
            URI uri = new URI(longUrl);
            if (uri.getScheme() == null || uri.getScheme().isBlank()) {
                throw new IllegalArgumentException("URL must have a scheme");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + e.getMessage());
        }

        String shortCode = idGenerator.nextId();

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setLongUrl(longUrl);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0);

        urlRepository.save(url);
        cacheService.put(shortCode, longUrl);

        String shortUrl = appProperties.getBaseUrl() + "/" + shortCode;
        return new ShortenResponse(shortUrl, shortCode, longUrl);
    }

    public Optional<String> resolveShortCode(String shortCode) {
        Optional<String> cached = cacheService.get(shortCode);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);
        if (urlOpt.isPresent()) {
            String longUrl = urlOpt.get().getLongUrl();
            cacheService.put(shortCode, longUrl);
            return Optional.of(longUrl);
        }
        return Optional.empty();
    }

    public Optional<StatsResponse> getStats(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(url -> new StatsResponse(
                        url.getShortCode(),
                        url.getLongUrl(),
                        url.getClickCount(),
                        url.getCreatedAt()));
    }
}
