package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;

public class AppSettingsDto {
    private String paystackPublicKey;
    private String paystackSecretKey;
    private String stripePublicKey;
    private String stripeSecretKey;
    private String paystackLivePublicKey;
    private String paystackLiveSecretKey;
    private String stripeLivePublicKey;
    private String stripeLiveSecretKey;
    private String discordBotToken;
    private String slackBotToken;
    private String slackAppToken;
    private String slackSigningSecret;
    private boolean sandboxMode;

    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpFromEmail;
    private String smtpFromName;
    private boolean smtpUseTls;

    private String siteName;
    private String siteDescription;
    private String logoUrl;
    private String faviconUrl;

    private String aboutContent;
    private String contactContent;
    private String contactEmail;

    private LocalDateTime updatedAt;
    private String updatedBy;

    public AppSettingsDto() {}

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
