package com.jomea.urlshortener.repository;

import com.jomea.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderId(String authProvider, String providerId);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByCreatedAtDesc();

    Optional<User> findByDiscordId(String discordId);

    Optional<User> findByDiscordLinkCode(String discordLinkCode);

    boolean existsByDiscordLinkCode(String discordLinkCode);

    Optional<User> findBySlackId(String slackId);

    Optional<User> findBySlackLinkCode(String slackLinkCode);

    boolean existsBySlackLinkCode(String slackLinkCode);
}
