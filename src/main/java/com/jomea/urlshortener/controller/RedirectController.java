package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;
    private final UrlRepository urlRepository;

    public RedirectController(UrlService urlService, UrlRepository urlRepository) {
        this.urlService = urlService;
        this.urlRepository = urlRepository;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        var opt = urlService.resolveShortCode(shortCode);
        if (opt.isPresent()) {
            String url = opt.get();
            Thread.ofVirtual().start(() -> urlRepository.incrementClickCount(shortCode));
            return ResponseEntity.status(302).location(URI.create(url)).build();
        }
        return ResponseEntity.notFound().build();
    }
}
