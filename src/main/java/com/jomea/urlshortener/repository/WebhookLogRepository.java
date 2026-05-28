package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.WebhookLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    List<WebhookLog> findByWebhookIdOrderByCreatedAtDesc(Long webhookId);
    List<WebhookLog> findByWebhookId(Long webhookId, Pageable pageable);
}
