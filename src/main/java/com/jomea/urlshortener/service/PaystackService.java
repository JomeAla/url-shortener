package com.jomea.urlshortener.service;

import com.jomea.urlshortener.config.AesEncryption;
import com.jomea.urlshortener.repository.AppSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class PaystackService {

    private static final Logger log = LoggerFactory.getLogger(PaystackService.class);
    private static final String PAYSTACK_API = "https://api.paystack.co";

    private final AppSettingsRepository appSettingsRepository;
    private final AesEncryption aesEncryption;
    private final HttpClient httpClient;

    public PaystackService(AppSettingsRepository appSettingsRepository, AesEncryption aesEncryption) {
        this.appSettingsRepository = appSettingsRepository;
        this.aesEncryption = aesEncryption;
        this.httpClient = HttpClient.newHttpClient();
    }

    private String getSecretKey() {
        var settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null) return null;
        String encrypted = settings.isSandboxMode() ? settings.getPaystackSecretKey() : settings.getPaystackLiveSecretKey();
        if (encrypted == null) return null;
        return aesEncryption.decrypt(encrypted);
    }

    public String initializeTransaction(String email, String amountKobo, String callbackUrl) throws Exception {
        String secret = getSecretKey();
        if (secret == null) throw new IllegalStateException("Payment not configured");

        String json = "{\"email\":\"" + email + "\",\"amount\":\"" + amountKobo + "\",\"callback_url\":\"" + callbackUrl + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(PAYSTACK_API + "/transaction/initialize"))
            .header("Authorization", "Bearer " + secret)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new RuntimeException("Paystack error: " + res.body());

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var root = mapper.readTree(res.body());
        if (!root.get("status").asBoolean()) throw new RuntimeException("Paystack: " + root.get("message").asText());

        return root.get("data").get("authorization_url").asText();
    }

    public boolean verifyTransaction(String reference) throws Exception {
        String secret = getSecretKey();
        if (secret == null) throw new IllegalStateException("Payment not configured");

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(PAYSTACK_API + "/transaction/verify/" + reference))
            .header("Authorization", "Bearer " + secret)
            .GET()
            .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return false;

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var root = mapper.readTree(res.body());
        return root.get("status").asBoolean() && "success".equals(root.get("data").get("status").asText());
    }
}
