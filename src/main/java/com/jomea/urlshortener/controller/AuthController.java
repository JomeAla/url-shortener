package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.dto.AuthResponse;
import com.jomea.urlshortener.dto.LoginRequest;
import com.jomea.urlshortener.dto.RegisterRequest;
import com.jomea.urlshortener.entity.PasswordResetToken;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.PasswordResetTokenRepository;
import com.jomea.urlshortener.repository.UserRepository;
import com.jomea.urlshortener.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository,
                          EmailService emailService,
                          PasswordResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.emailService = emailService;
        this.resetTokenRepository = resetTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        boolean isFirst = userRepository.count() == 0;
        String role = isFirst ? "ADMIN" : "USER";

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);
        if (!isFirst) user.setTier("free");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        if (!isFirst) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        }

        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new AuthResponse(user.getEmail(), user.getName(), user.getRole(), user.getTier(), user.getAuthProvider()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);

            var user = userRepository.findByEmail(request.email()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(user.getEmail(), user.getName(), user.getRole(), user.getTier(), user.getAuthProvider()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "user", new AuthResponse(user.getEmail(), user.getName(), user.getRole(), user.getTier(), user.getAuthProvider())
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        SecurityContextHolder.clearContext();
        httpRequest.getSession(false);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email required"));

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link was sent"));

        SecureRandom rng = new SecureRandom();
        byte[] raw = new byte[32];
        rng.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUserId(user.getId());
        prt.setExpiry(LocalDateTime.now().plusHours(1));
        prt.setCreatedAt(LocalDateTime.now());
        resetTokenRepository.save(prt);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String resetLink = baseUrl + "/?reset_token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link was sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String password = body.get("password");
        if (token == null || password == null || password.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token or weak password"));

        PasswordResetToken prt = resetTokenRepository.findByTokenAndUsedFalse(token).orElse(null);
        if (prt == null || prt.getExpiry().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));

        User user = userRepository.findById(prt.getUserId()).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        prt.setUsed(true);
        resetTokenRepository.save(prt);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}
