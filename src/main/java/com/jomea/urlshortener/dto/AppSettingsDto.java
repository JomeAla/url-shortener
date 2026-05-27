package com.jomea.urlshortener.dto;

import java.time.LocalDateTime;

public class AppSettingsDto {
    private String paymentProvider;
    private String paymentPublicKey;
    private String paymentSecretKey;
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

    private LocalDateTime updatedAt;
    private String updatedBy;

    public AppSettingsDto() {}

    public String getPaymentProvider() { return paymentProvider; }
    public void setPaymentProvider(String paymentProvider) { this.paymentProvider = paymentProvider; }
    public String getPaymentPublicKey() { return paymentPublicKey; }
    public void setPaymentPublicKey(String paymentPublicKey) { this.paymentPublicKey = paymentPublicKey; }
    public String getPaymentSecretKey() { return paymentSecretKey; }
    public void setPaymentSecretKey(String paymentSecretKey) { this.paymentSecretKey = paymentSecretKey; }
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
