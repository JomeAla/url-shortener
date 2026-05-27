package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.ApiKey;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.ApiKeyRepository;
import com.jomea.urlshortener.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    public ApiKeyController(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> listKeys(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        List<Map<String, Object>> keys = apiKeyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().map(k -> Map.<String, Object>of(
                "id", k.getId(), "name", k.getName(), "prefix", k.getKeyPrefix(),
                "lastUsedAt", k.getLastUsedAt(), "createdAt", k.getCreatedAt()
            )).toList();
        return ResponseEntity.ok(keys);
    }

    @PostMapping
    public ResponseEntity<?> createKey(@RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();

        String name = body.getOrDefault("name", "API Key");
        SecureRandom rng = new SecureRandom();
        byte[] rawKey = new byte[32];
        rng.nextBytes(rawKey);
        String rawKeyStr = Base64.getUrlEncoder().withoutPadding().encodeToString(rawKey);
        String prefix = rawKeyStr.substring(0, 8);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hash = HexFormat.of().formatHex(md.digest(rawKeyStr.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

            ApiKey key = new ApiKey();
            key.setUserId(user.getId());
            key.setName(name);
            key.setKeyPrefix(prefix);
            key.setKeyHash(hash);
            key.setCreatedAt(LocalDateTime.now());
            apiKeyRepository.save(key);

            return ResponseEntity.ok(Map.of(
                "id", key.getId(), "name", name, "prefix", prefix,
                "key", "sk_" + rawKeyStr,
                "message", "Save this key — it won't be shown again"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKey(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        apiKeyRepository.deleteByIdAndUserId(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Key deleted"));
    }
}
