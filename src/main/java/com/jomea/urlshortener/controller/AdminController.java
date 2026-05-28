package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.config.AesEncryption;
import com.jomea.urlshortener.config.AppProperties;
import com.jomea.urlshortener.dto.AppSettingsDto;
import com.jomea.urlshortener.dto.PlanDto;
import com.jomea.urlshortener.dto.UserDto;
import com.jomea.urlshortener.entity.AppSettings;
import com.jomea.urlshortener.entity.Coupon;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.PromoBanner;
import com.jomea.urlshortener.entity.Url;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import com.jomea.urlshortener.repository.CouponRepository;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.PromoBannerRepository;
import com.jomea.urlshortener.repository.UrlRepository;
import com.jomea.urlshortener.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final AppSettingsRepository appSettingsRepository;
    private final PromoBannerRepository promoBannerRepository;
    private final CouponRepository couponRepository;
    private final AesEncryption aesEncryption;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AdminController(UrlRepository urlRepository, UserRepository userRepository,
                           PlanRepository planRepository, AppSettingsRepository appSettingsRepository,
                           PromoBannerRepository promoBannerRepository, CouponRepository couponRepository,
                           AesEncryption aesEncryption, PasswordEncoder passwordEncoder,
                           AppProperties appProperties) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.appSettingsRepository = appSettingsRepository;
        this.promoBannerRepository = promoBannerRepository;
        this.couponRepository = couponRepository;
        this.aesEncryption = aesEncryption;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
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
                p.getMaxClicksPerUrl(), p.isCustomDomains(), p.isApiAccess(),
                p.isHasQrCodes(), p.isHasCustomCodes(), p.isHasBulkImport(),
                p.isHasAdvancedAnalytics(), p.isHasWebhooks(), p.isHasTeamAccess(),
                p.getMaxRequestsPerMinute(), p.getFeatures(),
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
            plan.setCurrency((String) body.getOrDefault("currency", appProperties.getDefaultCurrency()));
            plan.setBillingPeriod((String) body.getOrDefault("billingPeriod", "monthly"));
            plan.setMaxUrls(Integer.parseInt(body.getOrDefault("maxUrls", "0").toString()));
            plan.setMaxClicksPerUrl(Integer.parseInt(body.getOrDefault("maxClicksPerUrl", "0").toString()));
            plan.setCustomDomains(Boolean.parseBoolean(body.getOrDefault("customDomains", "false").toString()));
            plan.setApiAccess(Boolean.parseBoolean(body.getOrDefault("apiAccess", "false").toString()));
            plan.setHasQrCodes(Boolean.parseBoolean(body.getOrDefault("hasQrCodes", "false").toString()));
            plan.setHasCustomCodes(Boolean.parseBoolean(body.getOrDefault("hasCustomCodes", "false").toString()));
            plan.setHasBulkImport(Boolean.parseBoolean(body.getOrDefault("hasBulkImport", "false").toString()));
            plan.setHasAdvancedAnalytics(Boolean.parseBoolean(body.getOrDefault("hasAdvancedAnalytics", "false").toString()));
            plan.setHasWebhooks(Boolean.parseBoolean(body.getOrDefault("hasWebhooks", "false").toString()));
            plan.setHasTeamAccess(Boolean.parseBoolean(body.getOrDefault("hasTeamAccess", "false").toString()));
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
            if (body.containsKey("hasQrCodes")) plan.setHasQrCodes(Boolean.parseBoolean(body.get("hasQrCodes").toString()));
            if (body.containsKey("hasCustomCodes")) plan.setHasCustomCodes(Boolean.parseBoolean(body.get("hasCustomCodes").toString()));
            if (body.containsKey("hasBulkImport")) plan.setHasBulkImport(Boolean.parseBoolean(body.get("hasBulkImport").toString()));
            if (body.containsKey("hasAdvancedAnalytics")) plan.setHasAdvancedAnalytics(Boolean.parseBoolean(body.get("hasAdvancedAnalytics").toString()));
            if (body.containsKey("hasWebhooks")) plan.setHasWebhooks(Boolean.parseBoolean(body.get("hasWebhooks").toString()));
            if (body.containsKey("hasTeamAccess")) plan.setHasTeamAccess(Boolean.parseBoolean(body.get("hasTeamAccess").toString()));
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

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body) {
        try {
            String email = (String) body.get("email");
            if (email == null || userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists or invalid"));
            }
            User user = new User();
            user.setName((String) body.getOrDefault("name", "User"));
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
            user.setRole((String) body.getOrDefault("role", "USER"));
            user.setTier((String) body.getOrDefault("tier", "free"));
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
            if ("ADMIN".equals(user.getRole())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete admin accounts"));
            }
            userRepository.delete(user);
            return ResponseEntity.ok(Map.of("message", "User deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Promo Banners ---

    @GetMapping("/banners")
    public ResponseEntity<List<PromoBanner>> listBanners() {
        return ResponseEntity.ok(promoBannerRepository.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping("/banners")
    public ResponseEntity<?> createBanner(@RequestBody Map<String, Object> body) {
        try {
            PromoBanner b = new PromoBanner();
            b.setTitle((String) body.get("title"));
            b.setMessage((String) body.get("message"));
            b.setCtaText((String) body.getOrDefault("ctaText", ""));
            b.setCtaUrl((String) body.getOrDefault("ctaUrl", ""));
            b.setBgColor((String) body.getOrDefault("bgColor", "#3563e9"));
            b.setTextColor((String) body.getOrDefault("textColor", "#ffffff"));
            b.setPosition((String) body.getOrDefault("position", "top"));
            b.setShowTo((String) body.getOrDefault("showTo", "all"));
            b.setDismissible(Boolean.parseBoolean(body.getOrDefault("dismissible", "true").toString()));
            b.setActive(Boolean.parseBoolean(body.getOrDefault("active", "true").toString()));
            b.setCreatedAt(LocalDateTime.now());
            if (body.containsKey("startDate")) b.setStartDate(LocalDateTime.parse((String) body.get("startDate")));
            if (body.containsKey("endDate")) b.setEndDate(LocalDateTime.parse((String) body.get("endDate")));
            promoBannerRepository.save(b);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Banner created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<?> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            PromoBanner b = promoBannerRepository.findById(id).orElseThrow();
            if (body.containsKey("title")) b.setTitle((String) body.get("title"));
            if (body.containsKey("message")) b.setMessage((String) body.get("message"));
            if (body.containsKey("ctaText")) b.setCtaText((String) body.get("ctaText"));
            if (body.containsKey("ctaUrl")) b.setCtaUrl((String) body.get("ctaUrl"));
            if (body.containsKey("bgColor")) b.setBgColor((String) body.get("bgColor"));
            if (body.containsKey("textColor")) b.setTextColor((String) body.get("textColor"));
            if (body.containsKey("position")) b.setPosition((String) body.get("position"));
            if (body.containsKey("showTo")) b.setShowTo((String) body.get("showTo"));
            if (body.containsKey("dismissible")) b.setDismissible(Boolean.parseBoolean(body.get("dismissible").toString()));
            if (body.containsKey("active")) b.setActive(Boolean.parseBoolean(body.get("active").toString()));
            if (body.containsKey("startDate")) b.setStartDate(LocalDateTime.parse((String) body.get("startDate")));
            if (body.containsKey("endDate")) b.setEndDate(LocalDateTime.parse((String) body.get("endDate")));
            promoBannerRepository.save(b);
            return ResponseEntity.ok(Map.of("message", "Banner updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) {
        try {
            promoBannerRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Banner deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Coupons ---

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> listCoupons() {
        return ResponseEntity.ok(couponRepository.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Map<String, Object> body) {
        try {
            String code = ((String) body.get("code")).toUpperCase().trim();
            if (couponRepository.findByCodeIgnoreCase(code).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon code already exists"));
            }
            Coupon c = new Coupon();
            c.setCode(code);
            c.setDiscountType((String) body.get("discountType"));
            c.setDiscountValue(new java.math.BigDecimal(body.get("discountValue").toString()));
            if (body.containsKey("maxUses")) c.setMaxUses(Integer.parseInt(body.get("maxUses").toString()));
            if (body.containsKey("minAmount")) c.setMinAmount(new java.math.BigDecimal(body.get("minAmount").toString()));
            if (body.containsKey("planSlug")) c.setPlanSlug((String) body.get("planSlug"));
            if (body.containsKey("expiresAt")) c.setExpiresAt(LocalDateTime.parse((String) body.get("expiresAt")));
            c.setActive(Boolean.parseBoolean(body.getOrDefault("active", "true").toString()));
            c.setCreatedAt(LocalDateTime.now());
            couponRepository.save(c);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Coupon created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Coupon c = couponRepository.findById(id).orElseThrow();
            if (body.containsKey("code")) c.setCode(((String) body.get("code")).toUpperCase().trim());
            if (body.containsKey("discountType")) c.setDiscountType((String) body.get("discountType"));
            if (body.containsKey("discountValue")) c.setDiscountValue(new java.math.BigDecimal(body.get("discountValue").toString()));
            if (body.containsKey("maxUses")) c.setMaxUses(Integer.parseInt(body.get("maxUses").toString()));
            if (body.containsKey("minAmount")) c.setMinAmount(new java.math.BigDecimal(body.get("minAmount").toString()));
            if (body.containsKey("planSlug")) c.setPlanSlug((String) body.get("planSlug"));
            if (body.containsKey("expiresAt")) c.setExpiresAt(LocalDateTime.parse((String) body.get("expiresAt")));
            if (body.containsKey("active")) c.setActive(Boolean.parseBoolean(body.get("active").toString()));
            couponRepository.save(c);
            return ResponseEntity.ok(Map.of("message", "Coupon updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            couponRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Coupon deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Settings ---

    private String mask(String value) {
        return (value == null || value.isEmpty()) ? "" : "*****";
    }

    @GetMapping("/settings")
    public ResponseEntity<AppSettingsDto> getSettings() {
        AppSettings s = appSettingsRepository.findById(1L).orElse(new AppSettings());
        AppSettingsDto dto = new AppSettingsDto();
        dto.setPaystackPublicKey(mask(s.getPaystackPublicKey()));
        dto.setPaystackSecretKey(mask(s.getPaystackSecretKey()));
        dto.setStripePublicKey(mask(s.getStripePublicKey()));
        dto.setStripeSecretKey(mask(s.getStripeSecretKey()));
        dto.setSandboxMode(s.isSandboxMode());
        dto.setPaystackLivePublicKey(mask(s.getPaystackLivePublicKey()));
        dto.setPaystackLiveSecretKey(mask(s.getPaystackLiveSecretKey()));
        dto.setStripeLivePublicKey(mask(s.getStripeLivePublicKey()));
        dto.setStripeLiveSecretKey(mask(s.getStripeLiveSecretKey()));
        dto.setDiscordBotToken(mask(s.getDiscordBotToken()));
        dto.setSlackBotToken(mask(s.getSlackBotToken()));
        dto.setSlackAppToken(mask(s.getSlackAppToken()));
        dto.setSlackSigningSecret(mask(s.getSlackSigningSecret()));
        dto.setSmtpHost(s.getSmtpHost());
        dto.setSmtpPort(s.getSmtpPort());
        dto.setSmtpUsername(mask(s.getSmtpUsername()));
        dto.setSmtpPassword(mask(s.getSmtpPassword()));
        dto.setSmtpFromEmail(s.getSmtpFromEmail());
        dto.setSmtpFromName(s.getSmtpFromName());
        dto.setSmtpUseTls(s.isSmtpUseTls());
        dto.setSiteName(s.getSiteName());
        dto.setSiteDescription(s.getSiteDescription());
        dto.setLogoUrl(s.getLogoUrl());
        dto.setFaviconUrl(s.getFaviconUrl());
        dto.setAboutContent(s.getAboutContent());
        dto.setContactContent(s.getContactContent());
        dto.setContactEmail(s.getContactEmail());
        dto.setUpdatedAt(s.getUpdatedAt());
        dto.setUpdatedBy(s.getUpdatedBy());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> body) {
        try {
            AppSettings s = appSettingsRepository.findById(1L).orElseGet(() -> {
                AppSettings ns = new AppSettings();
                ns.setId(1L);
                return ns;
            });

            if (body.containsKey("sandboxMode"))
                s.setSandboxMode(Boolean.parseBoolean(body.get("sandboxMode").toString()));

            // Paystack keys — only update if not masked
            if (body.containsKey("paystackPublicKey")) {
                String v = (String) body.get("paystackPublicKey");
                if (v != null && !v.contains("*****"))
                    s.setPaystackPublicKey(aesEncryption.encrypt(v));
            }
            if (body.containsKey("paystackSecretKey")) {
                String v = (String) body.get("paystackSecretKey");
                if (v != null && !v.contains("*****"))
                    s.setPaystackSecretKey(aesEncryption.encrypt(v));
            }

            // Stripe keys — only update if not masked
            if (body.containsKey("stripePublicKey")) {
                String v = (String) body.get("stripePublicKey");
                if (v != null && !v.contains("*****"))
                    s.setStripePublicKey(aesEncryption.encrypt(v));
            }
            if (body.containsKey("stripeSecretKey")) {
                String v = (String) body.get("stripeSecretKey");
                if (v != null && !v.contains("*****"))
                    s.setStripeSecretKey(aesEncryption.encrypt(v));
            }

            // Paystack live keys
            if (body.containsKey("paystackLivePublicKey")) {
                String v = (String) body.get("paystackLivePublicKey");
                if (v != null && !v.contains("*****"))
                    s.setPaystackLivePublicKey(aesEncryption.encrypt(v));
            }
            if (body.containsKey("paystackLiveSecretKey")) {
                String v = (String) body.get("paystackLiveSecretKey");
                if (v != null && !v.contains("*****"))
                    s.setPaystackLiveSecretKey(aesEncryption.encrypt(v));
            }

            // Stripe live keys
            if (body.containsKey("stripeLivePublicKey")) {
                String v = (String) body.get("stripeLivePublicKey");
                if (v != null && !v.contains("*****"))
                    s.setStripeLivePublicKey(aesEncryption.encrypt(v));
            }
            if (body.containsKey("stripeLiveSecretKey")) {
                String v = (String) body.get("stripeLiveSecretKey");
                if (v != null && !v.contains("*****"))
                    s.setStripeLiveSecretKey(aesEncryption.encrypt(v));
            }

            // Discord bot token
            if (body.containsKey("discordBotToken")) {
                String v = (String) body.get("discordBotToken");
                if (v != null && !v.contains("*****"))
                    s.setDiscordBotToken(aesEncryption.encrypt(v));
            }

            // Slack bot tokens
            if (body.containsKey("slackBotToken")) {
                String v = (String) body.get("slackBotToken");
                if (v != null && !v.contains("*****"))
                    s.setSlackBotToken(aesEncryption.encrypt(v));
            }
            if (body.containsKey("slackAppToken")) {
                String v = (String) body.get("slackAppToken");
                if (v != null && !v.contains("*****"))
                    s.setSlackAppToken(aesEncryption.encrypt(v));
            }
            if (body.containsKey("slackSigningSecret")) {
                String v = (String) body.get("slackSigningSecret");
                if (v != null && !v.contains("*****"))
                    s.setSlackSigningSecret(aesEncryption.encrypt(v));
            }

            if (body.containsKey("smtpHost"))
                s.setSmtpHost((String) body.get("smtpHost"));
            if (body.containsKey("smtpPort"))
                s.setSmtpPort(Integer.valueOf(body.get("smtpPort").toString()));
            if (body.containsKey("smtpFromEmail"))
                s.setSmtpFromEmail((String) body.get("smtpFromEmail"));
            if (body.containsKey("smtpFromName"))
                s.setSmtpFromName((String) body.get("smtpFromName"));
            if (body.containsKey("smtpUseTls"))
                s.setSmtpUseTls(Boolean.parseBoolean(body.get("smtpUseTls").toString()));

            // SMTP credentials — only update if not masked
            if (body.containsKey("smtpUsername")) {
                String v = (String) body.get("smtpUsername");
                if (v != null && !v.contains("*****"))
                    s.setSmtpUsername(aesEncryption.encrypt(v));
            }
            if (body.containsKey("smtpPassword")) {
                String v = (String) body.get("smtpPassword");
                if (v != null && !v.contains("*****"))
                    s.setSmtpPassword(aesEncryption.encrypt(v));
            }

            if (body.containsKey("siteName"))
                s.setSiteName((String) body.get("siteName"));
            if (body.containsKey("siteDescription"))
                s.setSiteDescription((String) body.get("siteDescription"));
            if (body.containsKey("aboutContent"))
                s.setAboutContent((String) body.get("aboutContent"));
            if (body.containsKey("contactContent"))
                s.setContactContent((String) body.get("contactContent"));
            if (body.containsKey("contactEmail"))
                s.setContactEmail((String) body.get("contactEmail"));

            s.setUpdatedAt(LocalDateTime.now());

            User currentUser = getCurrentUser();
            s.setUpdatedBy(currentUser != null ? currentUser.getEmail() : "admin");

            appSettingsRepository.save(s);
            return ResponseEntity.ok(Map.of("message", "Settings updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/settings/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("type") String type) {
        try {
            if (file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = type + "_" + System.currentTimeMillis() + ext;
            Path uploadDir = Path.of("uploads");
            Files.createDirectories(uploadDir);
            Path dest = uploadDir.resolve(filename);
            Files.write(dest, file.getBytes());

            String url = "/uploads/" + filename;

            AppSettings s = appSettingsRepository.findById(1L).orElseGet(() -> {
                AppSettings ns = new AppSettings();
                ns.setId(1L);
                return ns;
            });

            if ("logo".equals(type)) s.setLogoUrl(url);
            else if ("favicon".equals(type)) s.setFaviconUrl(url);
            else return ResponseEntity.badRequest().body(Map.of("error", "Unknown type: " + type));

            s.setUpdatedAt(LocalDateTime.now());
            User currentUser = getCurrentUser();
            s.setUpdatedBy(currentUser != null ? currentUser.getEmail() : "admin");
            appSettingsRepository.save(s);

            return ResponseEntity.ok(Map.of("url", url, "message", "File uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            return userRepository.findByEmail(((org.springframework.security.core.userdetails.User) principal).getUsername()).orElse(null);
        }
        return null;
    }
}
