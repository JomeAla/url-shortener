package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.ShortenRequest;
import com.jomea.urlshortener.dto.ShortenResponse;
import com.jomea.urlshortener.dto.StatsResponse;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShortenController {

    private final UrlService urlService;
    private final UrlRepository urlRepository;

    public ShortenController(UrlService urlService, UrlRepository urlRepository) {
        this.urlService = urlService;
        this.urlRepository = urlRepository;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@Valid @RequestBody ShortenRequest request) {
        try {
            ShortenResponse response = urlService.shortenUrl(request.url());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/urls")
    public ResponseEntity<List<Url>> listUrls() {
        return ResponseEntity.ok(urlRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/stats/{shortCode}")
    public ResponseEntity<?> stats(@PathVariable String shortCode) {
        return urlService.getStats(shortCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
