package com.jomea.urlshortener.service;

import com.jomea.urlshortener.entity.CustomDomain;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.CustomDomainRepository;
import com.jomea.urlshortener.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomDomainService {

    private static final Logger log = LoggerFactory.getLogger(CustomDomainService.class);
    private static final String TXT_PREFIX = "shrtly-verify=";

    private final CustomDomainRepository customDomainRepository;
    private final PlanRepository planRepository;
    private final DnsLookupService dnsLookupService;

    public CustomDomainService(CustomDomainRepository customDomainRepository,
                                PlanRepository planRepository,
                                DnsLookupService dnsLookupService) {
        this.customDomainRepository = customDomainRepository;
        this.planRepository = planRepository;
        this.dnsLookupService = dnsLookupService;
    }

    public List<CustomDomain> getUserDomains(Long userId) {
        return customDomainRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public CustomDomain addDomain(Long userId, String domain) {
        domain = domain.toLowerCase().trim();
        if (domain.startsWith("http://") || domain.startsWith("https://")) {
            throw new IllegalArgumentException("Enter the domain name without protocol (e.g. links.example.com)");
        }
        if (domain.contains("/")) {
            throw new IllegalArgumentException("Enter just the domain, not a full path");
        }
        if (customDomainRepository.existsByDomain(domain)) {
            throw new IllegalArgumentException("This domain is already registered");
        }
        String token = TXT_PREFIX + UUID.randomUUID().toString().replace("-", "");
        CustomDomain cd = new CustomDomain();
        cd.setUserId(userId);
        cd.setDomain(domain);
        cd.setVerificationToken(token);
        cd.setVerified(false);
        cd.setActive(true);
        cd.setCreatedAt(LocalDateTime.now());
        customDomainRepository.save(cd);
        log.info("Custom domain added: {} for user {}", domain, userId);
        return cd;
    }

    public boolean verifyDomain(Long domainId, Long userId) {
        CustomDomain cd = customDomainRepository.findById(domainId).orElse(null);
        if (cd == null) throw new IllegalArgumentException("Domain not found");
        if (!cd.getUserId().equals(userId)) throw new IllegalArgumentException("Domain not found");
        if (cd.isVerified()) return true;
        boolean found = dnsLookupService.verifyTxtRecord(cd.getDomain(), cd.getVerificationToken());
        if (found) {
            cd.setVerified(true);
            cd.setVerifiedAt(LocalDateTime.now());
            customDomainRepository.save(cd);
            log.info("Domain verified: {} for user {}", cd.getDomain(), userId);
        }
        return found;
    }

    public void removeDomain(Long domainId, Long userId) {
        CustomDomain cd = customDomainRepository.findById(domainId).orElse(null);
        if (cd == null) throw new IllegalArgumentException("Domain not found");
        if (!cd.getUserId().equals(userId)) throw new IllegalArgumentException("Domain not found");
        customDomainRepository.delete(cd);
        log.info("Custom domain removed: {} for user {}", cd.getDomain(), userId);
    }

    public Optional<CustomDomain> findVerifiedDomain(String domain) {
        return customDomainRepository.findByDomainAndVerifiedTrue(domain.toLowerCase().trim());
    }

    public void checkPlanEnforcesCustomDomains(User user) {
        if (user == null || user.getTier() == null) return;
        Plan plan = planRepository.findBySlug(user.getTier()).orElse(null);
        if (plan == null || !plan.isCustomDomains()) {
            throw new IllegalStateException("Custom domains are not available on your plan. Upgrade to Pro or Enterprise.");
        }
    }

    public static String getTxtPrefix() {
        return TXT_PREFIX;
    }
}
