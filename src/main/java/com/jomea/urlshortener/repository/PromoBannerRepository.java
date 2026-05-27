package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.PromoBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {
    List<PromoBanner> findAllByOrderByCreatedAtDesc();
}
