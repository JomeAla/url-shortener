package com.jomea.urlshortener.config;

import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PlanRepository planRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedPlans();
        seedUsers();
    }

    private void seedPlans() {
        if (planRepository.count() > 0) return;

        log.info("Seeding default plans...");

        Plan free = new Plan();
        free.setName("Free");
        free.setSlug("free");
        free.setDescription("For casual users getting started");
        free.setPrice(BigDecimal.ZERO);
        free.setCurrency("USD");
        free.setBillingPeriod("monthly");
        free.setMaxUrls(25);
        free.setMaxClicksPerUrl(1000);
        free.setCustomDomains(false);
        free.setApiAccess(true);
        free.setFeatures("[\"25 shortened URLs\",\"1,000 clicks per link\",\"Basic analytics\",\"QR codes\",\"API access\"]");
        free.setSortOrder(1);
        free.setActive(true);
        free.setCreatedAt(LocalDateTime.now());
        planRepository.save(free);

        Plan pro = new Plan();
        pro.setName("Pro");
        pro.setSlug("pro");
        pro.setDescription("For professionals and small teams");
        pro.setPrice(new BigDecimal("9.99"));
        pro.setCurrency("USD");
        pro.setBillingPeriod("monthly");
        pro.setMaxUrls(500);
        pro.setMaxClicksPerUrl(50000);
        pro.setCustomDomains(true);
        pro.setApiAccess(true);
        pro.setFeatures("[\"500 shortened URLs\",\"50,000 clicks per link\",\"Advanced analytics\",\"Custom domains\",\"Priority support\",\"CSV export\"]");
        pro.setSortOrder(2);
        pro.setActive(true);
        pro.setCreatedAt(LocalDateTime.now());
        planRepository.save(pro);

        Plan enterprise = new Plan();
        enterprise.setName("Enterprise");
        enterprise.setSlug("enterprise");
        enterprise.setDescription("For large teams and businesses");
        enterprise.setPrice(new BigDecimal("49.99"));
        enterprise.setCurrency("USD");
        enterprise.setBillingPeriod("monthly");
        enterprise.setMaxUrls(10000);
        enterprise.setMaxClicksPerUrl(0);
        enterprise.setCustomDomains(true);
        enterprise.setApiAccess(true);
        enterprise.setFeatures("[\"Unlimited URLs\",\"Unlimited clicks\",\"Full analytics suite\",\"Custom domains\",\"Team workspaces\",\"API key management\",\"Dedicated support\",\"Webhooks\"]");
        enterprise.setSortOrder(3);
        enterprise.setActive(true);
        enterprise.setCreatedAt(LocalDateTime.now());
        planRepository.save(enterprise);

        log.info("Seeded {} plans", planRepository.count());
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@shrtly.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@shrtly.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setTier("enterprise");
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            log.info("Seeded admin user: admin@shrtly.com / admin123");
        }

        if (!userRepository.existsByEmail("user@shrtly.com")) {
            User user = new User();
            user.setName("Test User");
            user.setEmail("user@shrtly.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            user.setTier("free");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Seeded test user: user@shrtly.com / user123");
        }
    }
}
