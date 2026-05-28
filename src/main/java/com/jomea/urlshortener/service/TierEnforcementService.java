package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

@Service
public class TierEnforcementService {

    private final PlanRepository planRepository;
    private final UrlRepository urlRepository;

    public TierEnforcementService(PlanRepository planRepository, UrlRepository urlRepository) {
        this.planRepository = planRepository;
        this.urlRepository = urlRepository;
    }

    public Plan resolvePlan(User user) {
        if (user == null || user.getTier() == null) return null;
        return planRepository.findBySlug(user.getTier()).orElse(null);
    }

    public void checkCanCreateUrl(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (plan.getMaxUrls() <= 0) return;
        long count = urlRepository.countByUserId(user.getId());
        if (count >= plan.getMaxUrls()) {
            throw new IllegalStateException("You've reached your plan limit of " + plan.getMaxUrls() + " URLs. Upgrade to create more.");
        }
    }

    public void checkCanAccessUrl(User user, long currentClickCount) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        int maxClicks = plan.getMaxClicksPerUrl();
        if (maxClicks <= 0) return;
        if (currentClickCount >= maxClicks) {
            throw new IllegalStateException("This link has reached its click limit under your plan. Upgrade for unlimited clicks.");
        }
    }

    public void checkApiAccess(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) throw new IllegalStateException("No plan assigned. Please set up a plan.");
        if (!plan.isApiAccess()) {
            throw new IllegalStateException("API access is not available on your current plan. Upgrade to Pro.");
        }
    }

    public void checkQrCodes(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (!plan.isHasQrCodes()) {
            throw new IllegalStateException("QR codes are not available on your current plan. Upgrade to Pro.");
        }
    }

    public void checkCustomCodes(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (!plan.isHasCustomCodes()) {
            throw new IllegalStateException("Custom codes, passwords, expiry, and advanced options are not available on your current plan. Upgrade to Pro.");
        }
    }

    public void checkBulkImport(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (!plan.isHasBulkImport()) {
            throw new IllegalStateException("Bulk import is not available on your current plan. Upgrade to Pro.");
        }
    }

    public void checkAdvancedAnalytics(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (!plan.isHasAdvancedAnalytics()) {
            throw new IllegalStateException("Advanced analytics are not available on your current plan. Upgrade to Pro.");
        }
    }

    public void checkWebhooks(User user) {
        Plan plan = resolvePlan(user);
        if (plan == null) return;
        if (!plan.isHasWebhooks()) {
            throw new IllegalStateException("Webhooks are not available on your current plan. Upgrade to Enterprise.");
        }
    }
}
