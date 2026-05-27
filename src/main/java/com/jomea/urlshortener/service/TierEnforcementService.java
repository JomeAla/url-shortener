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

    private Plan resolvePlan(User user) {
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
}
