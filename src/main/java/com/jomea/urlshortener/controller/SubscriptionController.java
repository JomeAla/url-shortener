package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.Coupon;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.entity.UserSubscription;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import com.jomea.urlshortener.repository.CouponRepository;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.repository.UserSubscriptionRepository;
import com.jomea.urlshortener.service.EmailService;
import com.jomea.urlshortener.service.PaystackService;
import com.jomea.urlshortener.service.StripeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final CouponRepository couponRepository;
    private final AppSettingsRepository appSettingsRepository;
    private final PaystackService paystackService;
    private final StripeService stripeService;
    private final EmailService emailService;

    public SubscriptionController(UserRepository userRepository, PlanRepository planRepository,
                                   UserSubscriptionRepository subscriptionRepository,
                                   CouponRepository couponRepository,
                                   AppSettingsRepository appSettingsRepository,
                                   PaystackService paystackService, StripeService stripeService,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.couponRepository = couponRepository;
        this.appSettingsRepository = appSettingsRepository;
        this.paystackService = paystackService;
        this.stripeService = stripeService;
        this.emailService = emailService;
    }

    private String getBaseUrl() {
        return "http://localhost:8081";
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<String, Boolean>> getProviders() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        boolean paystack = false;
        boolean stripe = false;
        if (settings != null) {
            String psKey = settings.isSandboxMode() ? settings.getPaystackSecretKey() : settings.getPaystackLiveSecretKey();
            String stKey = settings.isSandboxMode() ? settings.getStripeSecretKey() : settings.getStripeLiveSecretKey();
            paystack = psKey != null && !psKey.isBlank();
            stripe = stKey != null && !stKey.isBlank();
        }
        return ResponseEntity.ok(Map.of("paystack", paystack, "stripe", stripe));
    }

    @PostMapping("/initialize/{planSlug}")
    public ResponseEntity<?> initialize(@PathVariable String planSlug, @RequestBody(required = false) Map<String, Object> body,
                                         Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));

        Plan plan = planRepository.findBySlug(planSlug).orElse(null);
        if (plan == null || !plan.isActive())
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid plan"));

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        BigDecimal price = plan.getPrice();
        String couponCode = body != null ? (String) body.get("coupon") : null;
        String provider = body != null ? (String) body.get("provider") : "paystack";
        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode).orElse(null);
            if (coupon == null || !coupon.isActive())
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid coupon"));
            if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now()))
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon has expired"));
            if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses())
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon usage limit reached"));
            if (coupon.getPlanSlug() != null && !coupon.getPlanSlug().equals(planSlug))
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon not valid for this plan"));
            if (coupon.getMinAmount() != null && price.compareTo(coupon.getMinAmount()) < 0)
                return ResponseEntity.badRequest().body(Map.of("error", "Minimum amount not met for this coupon"));

            if ("percentage".equals(coupon.getDiscountType())) {
                discount = price.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"));
            } else {
                discount = coupon.getDiscountValue();
            }
            if (discount.compareTo(price) > 0) discount = price;

            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepository.save(coupon);
        }

        BigDecimal finalPrice = price.subtract(discount);

        if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            user.setTier(plan.getSlug());
            userRepository.save(user);
            UserSubscription sub = new UserSubscription();
            sub.setUserId(user.getId());
            sub.setPlanSlug(plan.getSlug());
            sub.setStatus("active");
            sub.setStartDate(LocalDateTime.now());
            sub.setAmount(BigDecimal.ZERO);
            sub.setCurrency(plan.getCurrency());
            sub.setCreatedAt(LocalDateTime.now());
            subscriptionRepository.save(sub);
            return ResponseEntity.ok(Map.of("status", "activated", "plan", plan.getSlug(), "discount", discount, "finalPrice", 0));
        }

        String baseUrl = getBaseUrl();

        if ("stripe".equals(provider)) {
            try {
                var sessionInfo = stripeService.createCheckoutSession(
                    user.getEmail(), plan.getSlug(), plan.getName(),
                    finalPrice, plan.getCurrency(), baseUrl);

                UserSubscription sub = new UserSubscription();
                sub.setUserId(user.getId());
                sub.setPlanSlug(plan.getSlug());
                sub.setStatus("pending");
                sub.setAmount(finalPrice);
                sub.setCurrency(plan.getCurrency());
                sub.setStripeSessionId(sessionInfo.get("id"));
                sub.setCreatedAt(LocalDateTime.now());
                subscriptionRepository.save(sub);

                return ResponseEntity.ok(Map.of(
                    "checkout_url", sessionInfo.get("url"),
                    "provider", "stripe",
                    "plan", plan.getSlug(),
                    "amount", finalPrice,
                    "originalPrice", price,
                    "discount", discount
                ));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        } else {
            try {
                String callbackUrl = baseUrl + "/?payment_callback=" + plan.getSlug();
                String amountKobo = finalPrice.multiply(new BigDecimal("100")).toBigInteger().toString();
                String authUrl = paystackService.initializeTransaction(user.getEmail(), amountKobo, callbackUrl);
                return ResponseEntity.ok(Map.of("authorization_url", authUrl, "provider", "paystack",
                    "plan", plan.getSlug(), "amount", finalPrice, "originalPrice", price, "discount", discount));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
    }

    @PostMapping("/validate-coupon")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String planSlug = body.get("planSlug");
        if (code == null || code.isBlank())
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Code required"));

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim()).orElse(null);
        if (coupon == null || !coupon.isActive())
            return ResponseEntity.ok(Map.of("valid", false, "error", "Invalid coupon code"));
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now()))
            return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon has expired"));
        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses())
            return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon usage limit reached"));
        if (coupon.getPlanSlug() != null && !coupon.getPlanSlug().equals(planSlug))
            return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon not valid for this plan"));

        Plan plan = planRepository.findBySlug(planSlug).orElse(null);
        BigDecimal price = plan != null ? plan.getPrice() : BigDecimal.ZERO;
        if (coupon.getMinAmount() != null && price.compareTo(coupon.getMinAmount()) < 0)
            return ResponseEntity.ok(Map.of("valid", false, "error", "Minimum amount not met"));

        String desc = "percentage".equals(coupon.getDiscountType())
            ? coupon.getDiscountValue() + "% off"
            : coupon.getDiscountValue().toString() + " off";
        return ResponseEntity.ok(Map.of("valid", true, "discountType", coupon.getDiscountType(),
            "discountValue", coupon.getDiscountValue(), "description", desc));
    }

    @PostMapping("/verify/{reference}")
    public ResponseEntity<?> verify(@PathVariable String reference, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        try {
            boolean valid = paystackService.verifyTransaction(reference);
            if (!valid) return ResponseEntity.badRequest().body(Map.of("error", "Payment verification failed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        UserSubscription sub = subscriptionRepository.findTopByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "active").orElse(null);
        if (sub == null) return ResponseEntity.badRequest().body(Map.of("error", "No pending subscription"));

        user.setTier(sub.getPlanSlug());
        userRepository.save(user);

        sub.setPaystackReference(reference);
        sub.setStatus("active");
        sub.setStartDate(LocalDateTime.now());
        subscriptionRepository.save(sub);

        Plan plan = planRepository.findBySlug(sub.getPlanSlug()).orElse(null);
        if (plan != null) {
            emailService.sendPaymentReceipt(user.getEmail(), user.getName(), plan.getName(),
                plan.getPrice().toString(), plan.getCurrency());
        }

        return ResponseEntity.ok(Map.of("status", "activated", "plan", sub.getPlanSlug()));
    }

    @PostMapping("/verify-stripe/{sessionId}")
    public ResponseEntity<?> verifyStripe(@PathVariable String sessionId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));

        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        boolean valid = stripeService.verifySession(sessionId);
        if (!valid) return ResponseEntity.badRequest().body(Map.of("error", "Payment verification failed"));

        UserSubscription sub = subscriptionRepository
            .findTopByUserIdAndStripeSessionIdOrderByCreatedAtDesc(user.getId(), sessionId)
            .orElse(null);
        if (sub == null) return ResponseEntity.badRequest().body(Map.of("error", "No pending subscription"));

        user.setTier(sub.getPlanSlug());
        userRepository.save(user);

        sub.setStatus("active");
        sub.setStartDate(LocalDateTime.now());
        subscriptionRepository.save(sub);

        Plan plan = planRepository.findBySlug(sub.getPlanSlug()).orElse(null);
        if (plan != null) {
            emailService.sendPaymentReceipt(user.getEmail(), user.getName(), plan.getName(),
                plan.getPrice().toString(), plan.getCurrency());
        }

        return ResponseEntity.ok(Map.of("status", "activated", "plan", sub.getPlanSlug()));
    }
}
