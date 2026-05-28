package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.Webhook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    List<Webhook> findByUserId(Long userId);
    Optional<Webhook> findByIdAndUserId(Long id, Long userId);
    List<Webhook> findByUserIdAndActiveTrue(Long userId);
}
