package com.jomea.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_settings")
public class AppSettings {

    @Id
    private Long id;

    @Column(name = "paystack_public_key", columnDefinition = "TEXT")
    private String paystackPublicKey;

    @Column(name = "paystack_secret_key", columnDefinition = "TEXT")
    private String paystackSecretKey;

    @Column(name = "stripe_public_key", columnDefinition = "TEXT")
    private String stripePublicKey;

    @Column(name = "stripe_secret_key", columnDefinition = "TEXT")
    private String stripeSecretKey;

    @Column(name = "paystack_live_public_key", columnDefinition = "TEXT")
    private String paystackLivePublicKey;

    @Column(name = "paystack_live_secret_key", columnDefinition = "TEXT")
    private String paystackLiveSecretKey;

    @Column(name = "stripe_live_public_key", columnDefinition = "TEXT")
    private String stripeLivePublicKey;

    @Column(name = "stripe_live_secret_key", columnDefinition = "TEXT")
    private String stripeLiveSecretKey;

    @Column(name = "discord_bot_token", columnDefinition = "TEXT")
    private String discordBotToken;

    @Column(name = "slack_bot_token", columnDefinition = "TEXT")
    private String slackBotToken;

    @Column(name = "slack_app_token", columnDefinition = "TEXT")
    private String slackAppToken;

    @Column(name = "slack_signing_secret", columnDefinition = "TEXT")
    private String slackSigningSecret;

    @Column(name = "sandbox_mode", nullable = false)
    private boolean sandboxMode = true;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "smtp_from_email")
    private String smtpFromEmail;

    @Column(name = "smtp_from_name")
    private String smtpFromName;

    @Column(name = "smtp_use_tls")
    private boolean smtpUseTls = true;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_description", columnDefinition = "TEXT")
    private String siteDescription;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "favicon_url", columnDefinition = "TEXT")
    private String faviconUrl;

    @Column(name = "about_content", columnDefinition = "TEXT")
    private String aboutContent;

    @Column(name = "contact_content", columnDefinition = "TEXT")
    private String contactContent;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public AppSettings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaystackPublicKey() { return paystackPublicKey; }
    public void setPaystackPublicKey(String paystackPublicKey) { this.paystackPublicKey = paystackPublicKey; }
    public String getPaystackSecretKey() { return paystackSecretKey; }
    public void setPaystackSecretKey(String paystackSecretKey) { this.paystackSecretKey = paystackSecretKey; }
    public String getStripePublicKey() { return stripePublicKey; }
    public void setStripePublicKey(String stripePublicKey) { this.stripePublicKey = stripePublicKey; }
    public String getStripeSecretKey() { return stripeSecretKey; }
    public void setStripeSecretKey(String stripeSecretKey) { this.stripeSecretKey = stripeSecretKey; }
    public String getPaystackLivePublicKey() { return paystackLivePublicKey; }
    public void setPaystackLivePublicKey(String paystackLivePublicKey) { this.paystackLivePublicKey = paystackLivePublicKey; }
    public String getPaystackLiveSecretKey() { return paystackLiveSecretKey; }
    public void setPaystackLiveSecretKey(String paystackLiveSecretKey) { this.paystackLiveSecretKey = paystackLiveSecretKey; }
    public String getStripeLivePublicKey() { return stripeLivePublicKey; }
    public void setStripeLivePublicKey(String stripeLivePublicKey) { this.stripeLivePublicKey = stripeLivePublicKey; }
    public String getStripeLiveSecretKey() { return stripeLiveSecretKey; }
    public void setStripeLiveSecretKey(String stripeLiveSecretKey) { this.stripeLiveSecretKey = stripeLiveSecretKey; }
    public String getDiscordBotToken() { return discordBotToken; }
    public void setDiscordBotToken(String discordBotToken) { this.discordBotToken = discordBotToken; }
    public String getSlackBotToken() { return slackBotToken; }
    public void setSlackBotToken(String slackBotToken) { this.slackBotToken = slackBotToken; }
    public String getSlackAppToken() { return slackAppToken; }
    public void setSlackAppToken(String slackAppToken) { this.slackAppToken = slackAppToken; }
    public String getSlackSigningSecret() { return slackSigningSecret; }
    public void setSlackSigningSecret(String slackSigningSecret) { this.slackSigningSecret = slackSigningSecret; }
    public boolean isSandboxMode() { return sandboxMode; }
    public void setSandboxMode(boolean sandboxMode) { this.sandboxMode = sandboxMode; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }
    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }
    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    public String getSmtpFromEmail() { return smtpFromEmail; }
    public void setSmtpFromEmail(String smtpFromEmail) { this.smtpFromEmail = smtpFromEmail; }
    public String getSmtpFromName() { return smtpFromName; }
    public void setSmtpFromName(String smtpFromName) { this.smtpFromName = smtpFromName; }
    public boolean isSmtpUseTls() { return smtpUseTls; }
    public void setSmtpUseTls(boolean smtpUseTls) { this.smtpUseTls = smtpUseTls; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getSiteDescription() { return siteDescription; }
    public void setSiteDescription(String siteDescription) { this.siteDescription = siteDescription; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getFaviconUrl() { return faviconUrl; }
    public void setFaviconUrl(String faviconUrl) { this.faviconUrl = faviconUrl; }
    public String getAboutContent() { return aboutContent; }
    public void setAboutContent(String aboutContent) { this.aboutContent = aboutContent; }
    public String getContactContent() { return contactContent; }
    public void setContactContent(String contactContent) { this.contactContent = contactContent; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
