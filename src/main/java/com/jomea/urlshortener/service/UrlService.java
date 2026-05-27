package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.dto.BulkShortenRequest;
import com.jomea.urlshortener.dto.BulkShortenResponseItem;
import com.jomea.urlshortener.dto.ShortenResponse;
import com.jomea.urlshortener.dto.StatsResponse;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.UrlRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final IdGenerator idGenerator;
    private final CacheService cacheService;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;

    public UrlService(UrlRepository urlRepository, IdGenerator idGenerator,
                      CacheService cacheService, AppProperties appProperties,
                      PasswordEncoder passwordEncoder) {
        this.urlRepository = urlRepository;
        this.idGenerator = idGenerator;
        this.cacheService = cacheService;
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
    }

    public ShortenResponse shortenUrl(String longUrl, String customCode, String expiresAt, String password) {
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

        if (customCode != null && !customCode.isBlank()) {
            if (!isValidCustomCode(customCode)) {
                throw new IllegalArgumentException("Custom code must be alphanumeric and 4-20 characters");
            }
            if (urlRepository.findByCustomCode(customCode).isPresent()) {
                throw new IllegalArgumentException("Custom code already taken");
            }
        }

        String shortCode;
        if (customCode != null && !customCode.isBlank()) {
            shortCode = customCode;
        } else {
            shortCode = idGenerator.nextId();
        }

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setLongUrl(longUrl);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0);

        if (customCode != null && !customCode.isBlank()) {
            url.setCustomCode(customCode);
        }

        if (expiresAt != null && !expiresAt.isBlank()) {
            url.setExpiresAt(LocalDateTime.parse(expiresAt));
        }

        if (password != null && !password.isBlank()) {
            url.setPasswordHash(passwordEncoder.encode(password));
        }

        urlRepository.save(url);
        cacheService.put(shortCode, longUrl);

        String shortUrl = appProperties.getBaseUrl() + "/" + shortCode;
        boolean hasPassword = password != null && !password.isBlank();
        boolean isCustom = customCode != null && !customCode.isBlank();
        return new ShortenResponse(shortUrl, shortCode, longUrl, expiresAt, hasPassword, isCustom);
    }

    public List<BulkShortenResponseItem> shortenBulk(List<BulkShortenRequest> requests) {
        List<BulkShortenResponseItem> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            BulkShortenRequest req = requests.get(i);
            try {
                ShortenResponse response = shortenUrl(req.url(), req.customCode(), req.expiresAt(), req.password());
                results.add(new BulkShortenResponseItem(
                    i, "success", response.shortCode(), null, response.shortUrl(), response.longUrl()
                ));
            } catch (Exception e) {
                results.add(new BulkShortenResponseItem(
                    i, "error", null, e.getMessage(), null, req.url()
                ));
            }
        }
        return results;
    }

    public List<Url> searchUrls(String query, String dateFrom, String dateTo) {
        if (query != null && !query.isBlank() && dateFrom != null && !dateFrom.isBlank() && dateTo != null && !dateTo.isBlank()) {
            LocalDateTime from = LocalDateTime.parse(dateFrom);
            LocalDateTime to = LocalDateTime.parse(dateTo);
            return urlRepository.findByShortCodeContainingOrLongUrlContainingAllIgnoreCase(query, query, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(from) && !u.getCreatedAt().isAfter(to))
                .toList();
        } else if (query != null && !query.isBlank()) {
            return urlRepository.findByShortCodeContainingOrLongUrlContainingAllIgnoreCase(query, query, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (dateFrom != null && !dateFrom.isBlank() && dateTo != null && !dateTo.isBlank()) {
            LocalDateTime from = LocalDateTime.parse(dateFrom);
            LocalDateTime to = LocalDateTime.parse(dateTo);
            return urlRepository.findByCreatedAtBetween(from, to, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            return urlRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    public Url updateLink(String shortCode, String newLongUrl, String customCode, String expiresAt, String password) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));

        if (newLongUrl != null && !newLongUrl.isBlank()) {
            if (newLongUrl.length() > appProperties.getMaxUrlLength()) {
                throw new IllegalArgumentException("URL exceeds maximum length");
            }
            url.setLongUrl(newLongUrl);
        }

        if (customCode != null && !customCode.isBlank() && !customCode.equals(url.getCustomCode())) {
            if (!isValidCustomCode(customCode)) {
                throw new IllegalArgumentException("Custom code must be alphanumeric and 4-20 characters");
            }
            if (urlRepository.findByCustomCode(customCode).isPresent()
                    && !customCode.equals(url.getCustomCode())) {
                throw new IllegalArgumentException("Custom code already taken");
            }
            url.setCustomCode(customCode);
            url.setShortCode(customCode);
        } else if (customCode != null && customCode.isBlank() && url.getCustomCode() != null) {
            url.setCustomCode(null);
        }

        if (expiresAt != null && !expiresAt.isBlank()) {
            url.setExpiresAt(LocalDateTime.parse(expiresAt));
        } else if (expiresAt != null && expiresAt.isBlank()) {
            url.setExpiresAt(null);
        }

        if (password != null && !password.isBlank()) {
            url.setPasswordHash(passwordEncoder.encode(password));
        } else if (password != null && password.isBlank()) {
            url.setPasswordHash(null);
        }

        urlRepository.save(url);
        cacheService.put(url.getShortCode(), url.getLongUrl());
        return url;
    }

    public void deleteLink(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        urlRepository.delete(url);
        cacheService.invalidate(shortCode);
    }

    public Optional<String> resolveShortCode(String shortCode) {
        Optional<String> cached = cacheService.get(shortCode);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);
        if (urlOpt.isPresent()) {
            Url url = urlOpt.get();
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("This link has expired");
            }
            String longUrl = url.getLongUrl();
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

    public Optional<Url> getShortCodeDetails(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }

    public boolean verifyPassword(String shortCode, String password) {
        return urlRepository.findByShortCode(shortCode)
                .map(url -> url.getPasswordHash() != null && passwordEncoder.matches(password, url.getPasswordHash()))
                .orElse(false);
    }

    public Optional<String> resolveWithPassword(String shortCode, String password) {
        return urlRepository.findByShortCode(shortCode)
                .filter(url -> url.getPasswordHash() == null || passwordEncoder.matches(password, url.getPasswordHash()))
                .map(url -> {
                    cacheService.put(shortCode, url.getLongUrl());
                    return url.getLongUrl();
                });
    }

    private boolean isValidCustomCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{4,20}$");
    }
}
