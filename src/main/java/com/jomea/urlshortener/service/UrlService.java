package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.dto.BulkShortenRequest;
import com.jomea.urlshortener.dto.BulkShortenResponseItem;
import com.jomea.urlshortener.dto.ShortenResponse;
import com.jomea.urlshortener.dto.StatsResponse;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final IdGenerator idGenerator;
    private final CacheService cacheService;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TierEnforcementService tierEnforcement;

    public UrlService(UrlRepository urlRepository, IdGenerator idGenerator,
                      CacheService cacheService, AppProperties appProperties,
                      PasswordEncoder passwordEncoder, UserRepository userRepository,
                      TierEnforcementService tierEnforcement) {
        this.urlRepository = urlRepository;
        this.idGenerator = idGenerator;
        this.cacheService = cacheService;
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tierEnforcement = tierEnforcement;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public ShortenResponse shortenUrl(String longUrl, String customCode, String expiresAt, String password,
                                       String tags, String utmSource, String utmMedium, String utmCampaign,
                                       String utmTerm, String utmContent, Long folderId, Long workspaceId,
                                       String ogTitle, String ogDescription, String ogImage) {
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

        User user = getCurrentUser();
        if (user != null) {
            tierEnforcement.checkCanCreateUrl(user);
        }

        String shortCode;
        if (customCode != null && !customCode.isBlank()) {
            shortCode = customCode;
        } else {
            shortCode = idGenerator.nextId();
        }

        StringBuilder finalUrl = new StringBuilder(longUrl);
        String querySep = longUrl.contains("?") ? "&" : "?";
        boolean addedUtm = false;
        if (utmSource != null && !utmSource.isBlank()) {
            finalUrl.append(querySep).append("utm_source=").append(encodeParam(utmSource));
            querySep = "&"; addedUtm = true;
        }
        if (utmMedium != null && !utmMedium.isBlank()) {
            finalUrl.append(querySep).append("utm_medium=").append(encodeParam(utmMedium));
            querySep = "&"; addedUtm = true;
        }
        if (utmCampaign != null && !utmCampaign.isBlank()) {
            finalUrl.append(querySep).append("utm_campaign=").append(encodeParam(utmCampaign));
            querySep = "&"; addedUtm = true;
        }
        if (utmTerm != null && !utmTerm.isBlank()) {
            finalUrl.append(querySep).append("utm_term=").append(encodeParam(utmTerm));
            querySep = "&"; addedUtm = true;
        }
        if (utmContent != null && !utmContent.isBlank()) {
            finalUrl.append(querySep).append("utm_content=").append(encodeParam(utmContent));
        }

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setLongUrl(finalUrl.toString());
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0);

        if (user != null) url.setUserId(user.getId());

        if (customCode != null && !customCode.isBlank()) {
            url.setCustomCode(customCode);
        }

        if (expiresAt != null && !expiresAt.isBlank()) {
            url.setExpiresAt(LocalDateTime.parse(expiresAt));
        }

        if (password != null && !password.isBlank()) {
            url.setPasswordHash(passwordEncoder.encode(password));
        }

        if (tags != null && !tags.isBlank()) url.setTags(tags.trim());
        if (utmSource != null && !utmSource.isBlank()) url.setUtmSource(utmSource);
        if (utmMedium != null && !utmMedium.isBlank()) url.setUtmMedium(utmMedium);
        if (utmCampaign != null && !utmCampaign.isBlank()) url.setUtmCampaign(utmCampaign);
        if (utmTerm != null && !utmTerm.isBlank()) url.setUtmTerm(utmTerm);
        if (utmContent != null && !utmContent.isBlank()) url.setUtmContent(utmContent);
        if (folderId != null) url.setFolderId(folderId);
        if (workspaceId != null) url.setWorkspaceId(workspaceId);
        if (ogTitle != null && !ogTitle.isBlank()) url.setOgTitle(ogTitle);
        if (ogDescription != null && !ogDescription.isBlank()) url.setOgDescription(ogDescription);
        if (ogImage != null && !ogImage.isBlank()) url.setOgImage(ogImage);

        urlRepository.save(url);
        cacheService.put(shortCode, finalUrl.toString());

        String shortUrl = appProperties.getBaseUrl() + "/" + shortCode;
        boolean hasPassword = password != null && !password.isBlank();
        boolean isCustom = customCode != null && !customCode.isBlank();
        return new ShortenResponse(shortUrl, shortCode, longUrl, expiresAt, hasPassword, isCustom,
            tags, utmSource, utmMedium, utmCampaign, utmTerm, utmContent);
    }

    private String encodeParam(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    public List<BulkShortenResponseItem> shortenBulk(List<BulkShortenRequest> requests) {
        List<BulkShortenResponseItem> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            BulkShortenRequest req = requests.get(i);
            try {
                ShortenResponse response = shortenUrl(req.url(), req.customCode(), req.expiresAt(), req.password(),
                    null, null, null, null, null, null, null, null, null, null, null);
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

    public List<Url> searchUrls(String query, String dateFrom, String dateTo, Long folderId, String tag, Long workspaceId) {
        User user = getCurrentUser();
        List<Url> all;
        if (query != null && !query.isBlank() && dateFrom != null && !dateFrom.isBlank() && dateTo != null && !dateTo.isBlank()) {
            LocalDateTime from = LocalDateTime.parse(dateFrom);
            LocalDateTime to = LocalDateTime.parse(dateTo);
            all = urlRepository.findByShortCodeContainingOrLongUrlContainingAllIgnoreCase(query, query, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(u -> u.getCreatedAt() != null && !u.getCreatedAt().isBefore(from) && !u.getCreatedAt().isAfter(to))
                .toList();
        } else if (query != null && !query.isBlank()) {
            all = urlRepository.findByShortCodeContainingOrLongUrlContainingAllIgnoreCase(query, query, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (dateFrom != null && !dateFrom.isBlank() && dateTo != null && !dateTo.isBlank()) {
            LocalDateTime from = LocalDateTime.parse(dateFrom);
            LocalDateTime to = LocalDateTime.parse(dateTo);
            all = urlRepository.findByCreatedAtBetween(from, to, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            all = urlRepository.findAllByOrderByCreatedAtDesc();
        }
        java.util.stream.Stream<Url> stream = all.stream();
        if (user != null) {
            stream = stream.filter(u -> (u.getUserId() == null || u.getUserId().equals(user.getId())) && u.getDeletedAt() == null);
        } else {
            stream = stream.filter(u -> u.getUserId() == null && u.getDeletedAt() == null);
        }
        if (workspaceId != null) {
            stream = stream.filter(u -> u.getWorkspaceId() != null && u.getWorkspaceId().equals(workspaceId));
        }
        if (folderId != null) {
            stream = stream.filter(u -> u.getFolderId() != null && u.getFolderId().equals(folderId));
        }
        if (tag != null && !tag.isBlank()) {
            String lowerTag = tag.toLowerCase();
            stream = stream.filter(u -> u.getTags() != null && u.getTags().toLowerCase().contains(lowerTag));
        }
        return stream.toList();
    }

    public Url updateLink(String shortCode, String newLongUrl, String customCode, String expiresAt, String password,
                           String tags, String utmSource, String utmMedium, String utmCampaign,
                           String utmTerm, String utmContent, Long folderId,
                           String ogTitle, String ogDescription, String ogImage) {
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

        if (tags != null) url.setTags(tags.isBlank() ? null : tags.trim());
        if (utmSource != null) url.setUtmSource(utmSource.isBlank() ? null : utmSource);
        if (utmMedium != null) url.setUtmMedium(utmMedium.isBlank() ? null : utmMedium);
        if (utmCampaign != null) url.setUtmCampaign(utmCampaign.isBlank() ? null : utmCampaign);
        if (utmTerm != null) url.setUtmTerm(utmTerm.isBlank() ? null : utmTerm);
        if (utmContent != null) url.setUtmContent(utmContent.isBlank() ? null : utmContent);
        if (folderId != null) url.setFolderId(folderId);
        if (ogTitle != null) url.setOgTitle(ogTitle.isBlank() ? null : ogTitle);
        if (ogDescription != null) url.setOgDescription(ogDescription.isBlank() ? null : ogDescription);
        if (ogImage != null) url.setOgImage(ogImage.isBlank() ? null : ogImage);

        urlRepository.save(url);
        cacheService.put(url.getShortCode(), url.getLongUrl());
        return url;
    }

    public void deleteLink(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        url.setDeletedAt(LocalDateTime.now());
        urlRepository.save(url);
        cacheService.invalidate(shortCode);
    }

    public void restoreLink(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        if (url.getDeletedAt() == null) throw new IllegalArgumentException("Link is not in trash");
        url.setDeletedAt(null);
        urlRepository.save(url);
    }

    public List<Url> getTrashedUrls() {
        User user = getCurrentUser();
        if (user == null) return List.of();
        return urlRepository.findTrashedByUserId(user.getId());
    }

    public void permanentDelete(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));
        urlRepository.delete(url);
        cacheService.invalidate(shortCode);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void purgeOldTrash() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Url> oldTrash = urlRepository.findDeletedBefore(cutoff);
        for (Url url : oldTrash) {
            urlRepository.delete(url);
            cacheService.invalidate(url.getShortCode());
        }
    }

    public Url shortenUrlEntity(Long userId, String longUrl, String customCode) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }
        if (customCode != null && !customCode.isBlank()) {
            if (!isValidCustomCode(customCode)) {
                throw new IllegalArgumentException("Custom code must be alphanumeric and 4-20 characters");
            }
            if (urlRepository.findByCustomCode(customCode).isPresent()) {
                throw new IllegalArgumentException("Custom code already taken");
            }
        }
        String shortCode = (customCode != null && !customCode.isBlank()) ? customCode : idGenerator.nextId();

        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "https://" + longUrl;
        }

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setLongUrl(longUrl);
        url.setUserId(userId);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0);
        if (customCode != null && !customCode.isBlank()) {
            url.setCustomCode(customCode);
        }
        urlRepository.save(url);
        cacheService.put(shortCode, longUrl);
        return url;
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
