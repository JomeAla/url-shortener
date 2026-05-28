package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.DiscordBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/discord")
public class DiscordController {

    private final DiscordBotService discordBotService;
    private final UserRepository userRepository;

    public DiscordController(DiscordBotService discordBotService, UserRepository userRepository) {
        this.discordBotService = discordBotService;
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "running", discordBotService.isRunning(),
            "error", discordBotService.getLastError() != null ? discordBotService.getLastError() : ""
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<?> start() {
        String token = discordBotService.getDecryptedToken();
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bot token not configured. Set it in admin settings first."));
        }
        boolean started = discordBotService.startBot(token);
        if (started) {
            return ResponseEntity.ok(Map.of("message", "Bot started"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", discordBotService.getLastError()));
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        discordBotService.stopBot();
        return ResponseEntity.ok(Map.of("message", "Bot stopped"));
    }

    @PostMapping("/generate-link-code")
    public ResponseEntity<?> generateLinkCode(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        user.setDiscordLinkCode(code);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @GetMapping("/link-status")
    public ResponseEntity<?> linkStatus(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        return ResponseEntity.ok(Map.of(
            "linked", user.getDiscordId() != null,
            "discordId", user.getDiscordId() != null ? user.getDiscordId() : ""
        ));
    }
}
