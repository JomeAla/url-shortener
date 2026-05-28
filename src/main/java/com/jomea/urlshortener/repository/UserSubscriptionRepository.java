package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    Optional<UserSubscription> findTopByUserIdAndStripeSessionIdOrderByCreatedAtDesc(Long userId, String stripeSessionId);
    long countByUserIdAndStatus(Long userId, String status);
}
