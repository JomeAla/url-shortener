package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.PlanDto;
import com.jomea.urlshortener.dto.UserDto;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    public AdminController(UrlRepository urlRepository, UserRepository userRepository, PlanRepository planRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/urls")
    public ResponseEntity<List<Url>> listUrls() {
        return ResponseEntity.ok(urlRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> dtos = userRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(u -> new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getTier(), u.getCreatedAt()))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
            "totalUrls", urlRepository.count(),
            "totalClicks", urlRepository.sumClickCount(),
            "totalUsers", userRepository.count()
        ));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> listPlans() {
        List<PlanDto> dtos = planRepository.findAll().stream()
            .map(p -> new PlanDto(p.getId(), p.getName(), p.getSlug(), p.getDescription(),
                p.getPrice(), p.getCurrency(), p.getBillingPeriod(), p.getMaxUrls(),
                p.getMaxClicksPerUrl(), p.isCustomDomains(), p.isApiAccess(), p.getFeatures(),
                p.getSortOrder(), p.isActive(), p.getCreatedAt()))
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/plans")
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> body) {
        try {
            if (planRepository.existsBySlug((String) body.get("slug"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Slug already exists"));
            }
            Plan plan = new Plan();
            plan.setName((String) body.get("name"));
            plan.setSlug((String) body.get("slug"));
            plan.setDescription((String) body.getOrDefault("description", ""));
            plan.setPrice(new java.math.BigDecimal(body.get("price").toString()));
            plan.setCurrency((String) body.getOrDefault("currency", "USD"));
            plan.setBillingPeriod((String) body.getOrDefault("billingPeriod", "monthly"));
            plan.setMaxUrls(Integer.parseInt(body.getOrDefault("maxUrls", "0").toString()));
            plan.setMaxClicksPerUrl(Integer.parseInt(body.getOrDefault("maxClicksPerUrl", "0").toString()));
            plan.setCustomDomains(Boolean.parseBoolean(body.getOrDefault("customDomains", "false").toString()));
            plan.setApiAccess(Boolean.parseBoolean(body.getOrDefault("apiAccess", "false").toString()));
            plan.setFeatures((String) body.getOrDefault("features", "[]"));
            plan.setSortOrder(Integer.parseInt(body.getOrDefault("sortOrder", "0").toString()));
            plan.setActive(Boolean.parseBoolean(body.getOrDefault("active", "true").toString()));
            plan.setCreatedAt(LocalDateTime.now());
            planRepository.save(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Plan created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Plan plan = planRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
            if (body.containsKey("name")) plan.setName((String) body.get("name"));
            if (body.containsKey("slug")) plan.setSlug((String) body.get("slug"));
            if (body.containsKey("description")) plan.setDescription((String) body.get("description"));
            if (body.containsKey("price")) plan.setPrice(new java.math.BigDecimal(body.get("price").toString()));
            if (body.containsKey("currency")) plan.setCurrency((String) body.get("currency"));
            if (body.containsKey("billingPeriod")) plan.setBillingPeriod((String) body.get("billingPeriod"));
            if (body.containsKey("maxUrls")) plan.setMaxUrls(Integer.parseInt(body.get("maxUrls").toString()));
            if (body.containsKey("maxClicksPerUrl")) plan.setMaxClicksPerUrl(Integer.parseInt(body.get("maxClicksPerUrl").toString()));
            if (body.containsKey("customDomains")) plan.setCustomDomains(Boolean.parseBoolean(body.get("customDomains").toString()));
            if (body.containsKey("apiAccess")) plan.setApiAccess(Boolean.parseBoolean(body.get("apiAccess").toString()));
            if (body.containsKey("features")) plan.setFeatures((String) body.get("features"));
            if (body.containsKey("sortOrder")) plan.setSortOrder(Integer.parseInt(body.get("sortOrder").toString()));
            if (body.containsKey("active")) plan.setActive(Boolean.parseBoolean(body.get("active").toString()));
            planRepository.save(plan);
            return ResponseEntity.ok(Map.of("message", "Plan updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        try {
            Plan plan = planRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
            planRepository.delete(plan);
            return ResponseEntity.ok(Map.of("message", "Plan deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
