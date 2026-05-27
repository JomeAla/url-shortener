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

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(name = "payment_public_key", columnDefinition = "TEXT")
    private String paymentPublicKey;

    @Column(name = "payment_secret_key", columnDefinition = "TEXT")
    private String paymentSecretKey;

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

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    public AppSettings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
