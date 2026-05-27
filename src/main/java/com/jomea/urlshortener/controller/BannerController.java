package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.PromoBanner;
import com.jomea.urlshortener.repository.PromoBannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BannerController {

    private final PromoBannerRepository bannerRepository;

    public BannerController(PromoBannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping("/banners")
    public ResponseEntity<List<PromoBanner>> getActiveBanners() {
        List<PromoBanner> banners = bannerRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .filter(PromoBanner::isActive)
            .filter(b -> b.getStartDate() == null || !b.getStartDate().isAfter(LocalDateTime.now()))
            .filter(b -> b.getEndDate() == null || !b.getEndDate().isBefore(LocalDateTime.now()))
            .toList();
        return ResponseEntity.ok(banners);
    }
}
