package com.jomea.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    private String tier;

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "discord_link_code", length = 32)
    private String discordLinkCode;

    @Column(name = "slack_id")
    private String slackId;

    @Column(name = "slack_team_id")
    private String slackTeamId;

    @Column(name = "slack_link_code", length = 32)
    private String slackLinkCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }
    public String getDiscordLinkCode() { return discordLinkCode; }
    public void setDiscordLinkCode(String discordLinkCode) { this.discordLinkCode = discordLinkCode; }
    public String getSlackId() { return slackId; }
    public void setSlackId(String slackId) { this.slackId = slackId; }
    public String getSlackTeamId() { return slackTeamId; }
    public void setSlackTeamId(String slackTeamId) { this.slackTeamId = slackTeamId; }
    public String getSlackLinkCode() { return slackLinkCode; }
    public void setSlackLinkCode(String slackLinkCode) { this.slackLinkCode = slackLinkCode; }
}
