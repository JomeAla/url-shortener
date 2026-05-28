package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.AppSettings;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PageController {

    private final AppSettingsRepository appSettingsRepository;

    public PageController(AppSettingsRepository appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    @GetMapping("/api/pages/content")
    public ResponseEntity<?> getPageContent(@RequestParam String page) {
        AppSettings s = appSettingsRepository.findById(1L).orElse(new AppSettings());
        if ("about".equals(page)) {
            return ResponseEntity.ok(Map.of(
                "title", "About Us",
                "content", s.getAboutContent() != null ? s.getAboutContent() : ""
            ));
        } else if ("contact".equals(page)) {
            return ResponseEntity.ok(Map.of(
                "title", "Contact Us",
                "content", s.getContactContent() != null ? s.getContactContent() : "",
                "email", s.getContactEmail() != null ? s.getContactEmail() : ""
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Unknown page"));
    }
}
