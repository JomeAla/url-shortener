package com.jomea.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "plan_seq")
    @SequenceGenerator(name = "plan_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false)
    private String billingPeriod = "monthly";

    @Column(nullable = false)
    private int maxUrls;

    @Column(nullable = false)
    private int maxClicksPerUrl;

    @Column(nullable = false)
    private boolean customDomains;

    @Column(nullable = false)
    private boolean apiAccess;

    @Column(name = "has_qr_codes", nullable = false)
    private boolean hasQrCodes;

    @Column(name = "has_custom_codes", nullable = false)
    private boolean hasCustomCodes;

    @Column(name = "has_bulk_import", nullable = false)
    private boolean hasBulkImport;

    @Column(name = "has_advanced_analytics", nullable = false)
    private boolean hasAdvancedAnalytics;

    @Column(name = "has_webhooks", nullable = false)
    private boolean hasWebhooks;

    @Column(name = "has_team_access", nullable = false)
    private boolean hasTeamAccess;

    @Column(name = "max_requests_per_minute", nullable = false)
    private int maxRequestsPerMinute = 60;

    @Column(columnDefinition = "TEXT")
    private String features;

    @Column(name = "sort_order")
    private int sortOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Plan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getBillingPeriod() { return billingPeriod; }
    public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }
    public int getMaxUrls() { return maxUrls; }
    public void setMaxUrls(int maxUrls) { this.maxUrls = maxUrls; }
    public int getMaxClicksPerUrl() { return maxClicksPerUrl; }
    public void setMaxClicksPerUrl(int maxClicksPerUrl) { this.maxClicksPerUrl = maxClicksPerUrl; }
    public boolean isCustomDomains() { return customDomains; }
    public void setCustomDomains(boolean customDomains) { this.customDomains = customDomains; }
    public boolean isApiAccess() { return apiAccess; }
    public void setApiAccess(boolean apiAccess) { this.apiAccess = apiAccess; }
    public boolean isHasQrCodes() { return hasQrCodes; }
    public void setHasQrCodes(boolean hasQrCodes) { this.hasQrCodes = hasQrCodes; }
    public boolean isHasCustomCodes() { return hasCustomCodes; }
    public void setHasCustomCodes(boolean hasCustomCodes) { this.hasCustomCodes = hasCustomCodes; }
    public boolean isHasBulkImport() { return hasBulkImport; }
    public void setHasBulkImport(boolean hasBulkImport) { this.hasBulkImport = hasBulkImport; }
    public boolean isHasAdvancedAnalytics() { return hasAdvancedAnalytics; }
    public void setHasAdvancedAnalytics(boolean hasAdvancedAnalytics) { this.hasAdvancedAnalytics = hasAdvancedAnalytics; }
    public boolean isHasWebhooks() { return hasWebhooks; }
    public void setHasWebhooks(boolean hasWebhooks) { this.hasWebhooks = hasWebhooks; }
    public boolean isHasTeamAccess() { return hasTeamAccess; }
    public void setHasTeamAccess(boolean hasTeamAccess) { this.hasTeamAccess = hasTeamAccess; }
    public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) { this.maxRequestsPerMinute = maxRequestsPerMinute; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
