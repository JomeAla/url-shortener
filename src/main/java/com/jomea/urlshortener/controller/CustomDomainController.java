package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.CustomDomain;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.CustomDomainService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-domains")
public class CustomDomainController {

    private final CustomDomainService customDomainService;
    private final UserRepository userRepository;

    public CustomDomainController(CustomDomainService customDomainService, UserRepository userRepository) {
        this.customDomainService = customDomainService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> listDomains(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        List<CustomDomain> domains = customDomainService.getUserDomains(user.getId());
        return ResponseEntity.ok(domains);
    }

    @PostMapping
    public ResponseEntity<?> addDomain(@RequestBody Map<String, String> body, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        String domain = body.get("domain");
        if (domain == null || domain.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Domain is required"));
        }
        try {
            customDomainService.checkPlanEnforcesCustomDomains(user);
            CustomDomain cd = customDomainService.addDomain(user.getId(), domain);
            return ResponseEntity.ok(Map.of(
                "id", cd.getId(),
                "domain", cd.getDomain(),
                "verificationToken", cd.getVerificationToken(),
                "verified", cd.isVerified(),
                "createdAt", cd.getCreatedAt().toString()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyDomain(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        try {
            boolean verified = customDomainService.verifyDomain(id, user.getId());
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Domain verified successfully"));
            } else {
                return ResponseEntity.ok(Map.of("message", "Verification failed. Make sure the TXT record is set correctly and DNS has propagated."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeDomain(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        try {
            customDomainService.removeDomain(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Domain removed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
