package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.SlackBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/slack")
public class SlackController {

    private final SlackBotService slackBotService;
    private final UserRepository userRepository;

    public SlackController(SlackBotService slackBotService, UserRepository userRepository) {
        this.slackBotService = slackBotService;
        this.userRepository = userRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "running", slackBotService.isRunning(),
            "error", slackBotService.getLastError() != null ? slackBotService.getLastError() : ""
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<?> start() {
        String botToken = slackBotService.getDecryptedBotToken();
        String appToken = slackBotService.getDecryptedAppToken();
        if (botToken == null || botToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bot token not configured. Set it in admin settings first."));
        }
        if (appToken == null || appToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "App token not configured. Set it in admin settings first."));
        }
        boolean started = slackBotService.startBot(botToken, appToken);
        if (started) {
            return ResponseEntity.ok(Map.of("message", "Slack bot started"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", slackBotService.getLastError()));
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        slackBotService.stopBot();
        return ResponseEntity.ok(Map.of("message", "Slack bot stopped"));
    }

    @PostMapping("/generate-link-code")
    public ResponseEntity<?> generateLinkCode(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Login required"));
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        user.setSlackLinkCode(code);
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
            "linked", user.getSlackId() != null,
            "slackId", user.getSlackId() != null ? user.getSlackId() : ""
        ));
    }
}
