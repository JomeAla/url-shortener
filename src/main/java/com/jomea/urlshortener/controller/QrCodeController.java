package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.QrCodeService;
import com.jomea.urlshortener.service.TierEnforcementService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final UserRepository userRepository;
    private final TierEnforcementService tierEnforcement;

    public QrCodeController(QrCodeService qrCodeService, UserRepository userRepository,
                            TierEnforcementService tierEnforcement) {
        this.qrCodeService = qrCodeService;
        this.userRepository = userRepository;
        this.tierEnforcement = tierEnforcement;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> getQrCode(@PathVariable String shortCode, Authentication auth) {
        try {
            enforceQrAccess(auth);
            byte[] png = qrCodeService.generateQrCodePng(shortCode);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(png);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{shortCode}/base64")
    public ResponseEntity<?> getQrCodeBase64(@PathVariable String shortCode, Authentication auth) {
        try {
            enforceQrAccess(auth);
            String base64 = qrCodeService.generateQrCodeBase64(shortCode);
            return ResponseEntity.ok(Map.of("qrCode", base64));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    private void enforceQrAccess(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("QR codes are not available on your current plan. Upgrade to Pro.");
        }
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        tierEnforcement.checkQrCodes(user);
    }
}
