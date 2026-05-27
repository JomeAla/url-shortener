package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.ClickEvent;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.ClickEventRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.view.RedirectView;

@RestController
public class RedirectController {

    private final UrlService urlService;
    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;

    public RedirectController(UrlService urlService, UrlRepository urlRepository, ClickEventRepository clickEventRepository) {
        this.urlService = urlService;
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/index.html");
    }

    @GetMapping("/{shortCode:[0-9A-Za-z]{4,20}}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode, HttpServletRequest request) {
        var details = urlService.getShortCodeDetails(shortCode);
        if (details.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Url url = details.get();
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "This link has expired"));
        }
        if (url.getPasswordHash() != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("requiresPassword", true, "shortCode", shortCode));
        }
        Thread.ofVirtual().start(() -> {
            urlRepository.incrementClickCount(shortCode);
            ClickEvent event = new ClickEvent();
            event.setShortCode(shortCode);
            event.setTimestamp(java.time.LocalDateTime.now());
            event.setReferer(request.getHeader("Referer"));
            event.setUserAgent(request.getHeader("User-Agent"));
            event.setIpAddress(request.getRemoteAddr());
            clickEventRepository.save(event);
        });
        return ResponseEntity.status(302).location(URI.create(url.getLongUrl())).build();
    }
}
