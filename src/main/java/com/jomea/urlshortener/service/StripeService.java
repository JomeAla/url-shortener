package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AesEncryption;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    private final AppSettingsRepository appSettingsRepository;
    private final AesEncryption aesEncryption;

    public StripeService(AppSettingsRepository appSettingsRepository, AesEncryption aesEncryption) {
        this.appSettingsRepository = appSettingsRepository;
        this.aesEncryption = aesEncryption;
    }

    private String getSecretKey() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null) return null;
        String encrypted = settings.isSandboxMode() ? settings.getStripeSecretKey() : settings.getStripeLiveSecretKey();
        if (encrypted == null) return null;
        return aesEncryption.decrypt(encrypted);
    }

    @PostConstruct
    public void init() {
        String key = getSecretKey();
        if (key != null) {
            Stripe.apiKey = key;
        }
    }

    public Map<String, String> createCheckoutSession(String email, String planSlug, String planName,
                                                       BigDecimal amount, String currency, String callbackUrl) throws StripeException {
        String secret = getSecretKey();
        if (secret == null) throw new IllegalStateException("Stripe not configured");
        Stripe.apiKey = secret;

        long amountCents = amount.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(callbackUrl + "?stripe_success=" + planSlug)
            .setCancelUrl(callbackUrl + "?stripe_cancel=" + planSlug)
            .setCustomerEmail(email)
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(currency.toLowerCase())
                    .setUnitAmount(amountCents)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(planName)
                        .build())
                    .build())
                .build())
            .putAllMetadata(Map.of("planSlug", planSlug, "email", email))
            .build();

        Session session = Session.create(params);
        return Map.of("id", session.getId(), "url", session.getUrl());
    }

    public boolean verifySession(String sessionId) {
        try {
            String secret = getSecretKey();
            if (secret == null) return false;
            Stripe.apiKey = secret;

            Session session = Session.retrieve(sessionId);
            return "complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus());
        } catch (Exception e) {
            log.error("Stripe verify failed for {}", sessionId, e);
            return false;
        }
    }
}
