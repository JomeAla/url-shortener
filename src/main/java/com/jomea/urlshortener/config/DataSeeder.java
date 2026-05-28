package com.jomea.urlshortener.config;

import com.jomea.urlshortener.entity.AppSettings;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.AppSettingsRepository;
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
    private final AppSettingsRepository appSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public DataSeeder(UserRepository userRepository,
                      PlanRepository planRepository,
                      AppSettingsRepository appSettingsRepository,
                      PasswordEncoder passwordEncoder,
                      AppProperties appProperties) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.appSettingsRepository = appSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        seedPlans();
        seedUsers();
        seedSettings();
    }

    private void seedPlans() {
        if (planRepository.count() > 0) return;

        String currency = appProperties.getDefaultCurrency();
        log.info("Seeding default plans with currency: {}", currency);

        Plan free = new Plan();
        free.setName("Free");
        free.setSlug("free");
        free.setDescription("For casual users getting started");
        free.setPrice(BigDecimal.ZERO);
        free.setCurrency(currency);
        free.setBillingPeriod("monthly");
        free.setMaxUrls(25);
        free.setMaxClicksPerUrl(1000);
        free.setCustomDomains(false);
        free.setApiAccess(false);
        free.setHasQrCodes(false);
        free.setHasCustomCodes(false);
        free.setHasBulkImport(false);
        free.setHasAdvancedAnalytics(false);
        free.setHasWebhooks(false);
        free.setHasTeamAccess(false);
        free.setMaxRequestsPerMinute(30);
        free.setFeatures("[\"25 shortened URLs\",\"1,000 clicks per link\",\"Basic analytics\"]");
        free.setSortOrder(1);
        free.setActive(true);
        free.setCreatedAt(LocalDateTime.now());
        planRepository.save(free);

        Plan pro = new Plan();
        pro.setName("Pro");
        pro.setSlug("pro");
        pro.setDescription("For professionals and small teams");
        pro.setPrice(new BigDecimal("5000"));
        pro.setCurrency(currency);
        pro.setBillingPeriod("monthly");
        pro.setMaxUrls(500);
        pro.setMaxClicksPerUrl(50000);
        pro.setCustomDomains(true);
        pro.setApiAccess(true);
        pro.setHasQrCodes(true);
        pro.setHasCustomCodes(true);
        pro.setHasBulkImport(true);
        pro.setHasAdvancedAnalytics(true);
        pro.setHasWebhooks(false);
        pro.setHasTeamAccess(false);
        pro.setMaxRequestsPerMinute(200);
        pro.setFeatures("[\"500 shortened URLs\",\"50,000 clicks per link\",\"Custom short codes\",\"Link expiry dates\",\"Password protection\",\"Tags & folders\",\"UTM builder\",\"Bulk URL import\",\"Link editing & deletion\",\"Search & filter\",\"Trash / soft delete\",\"Advanced analytics\",\"GeoIP click maps\",\"CSV export\",\"QR codes + download\",\"REST API access\",\"API key management\",\"Custom domains\",\"Link dashboard\",\"Email notifications\",\"Coupon support\",\"Priority support\"]");
        pro.setSortOrder(2);
        pro.setActive(true);
        pro.setCreatedAt(LocalDateTime.now());
        planRepository.save(pro);

        Plan enterprise = new Plan();
        enterprise.setName("Enterprise");
        enterprise.setSlug("enterprise");
        enterprise.setDescription("For large teams and businesses");
        enterprise.setPrice(new BigDecimal("20000"));
        enterprise.setCurrency(currency);
        enterprise.setBillingPeriod("monthly");
        enterprise.setMaxUrls(10000);
        enterprise.setMaxClicksPerUrl(0);
        enterprise.setCustomDomains(true);
        enterprise.setApiAccess(true);
        enterprise.setHasQrCodes(true);
        enterprise.setHasCustomCodes(true);
        enterprise.setHasBulkImport(true);
        enterprise.setHasAdvancedAnalytics(true);
        enterprise.setHasWebhooks(true);
        enterprise.setHasTeamAccess(true);
        enterprise.setMaxRequestsPerMinute(1000);
        enterprise.setFeatures("[\"10,000 URLs\",\"Unlimited clicks\",\"All Pro features\",\"Custom short codes\",\"Link expiry & passwords\",\"Tags, folders & UTM\",\"Bulk URL import\",\"Full analytics suite\",\"Real-time analytics API\",\"QR codes + download\",\"REST API + API keys\",\"Custom domains\",\"Webhooks + delivery logs\",\"HMAC payload signing\",\"Team workspaces\",\"SSO / OAuth\",\"Dedicated support\",\"Browser extension\",\"Slack & Discord bots\",\"n8n / Zapier / Make\",\"Social preview (OG tags)\"]");
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
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            log.info("Seeded admin user: admin@shrtly.com / admin123 (platform owner, no tier)");
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
            log.info("Seeded test user: user@shrtly.com / user123 (tier: free)");
        }
    }

    private void seedSettings() {
        if (appSettingsRepository.existsById(1L)) return;
        AppSettings s = new AppSettings();
        s.setId(1L);
        s.setSandboxMode(true);
        s.setSmtpUseTls(true);
        s.setSiteName("Shrtly");
        s.setSiteDescription("URL Shortener — shorten, track, and manage your links");
        s.setUpdatedAt(LocalDateTime.now());
        s.setUpdatedBy("system");
        appSettingsRepository.save(s);
        log.info("Seeded default app settings");
    }
}
