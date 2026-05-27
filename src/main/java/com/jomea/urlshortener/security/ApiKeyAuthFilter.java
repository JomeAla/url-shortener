package com.jomea.urlshortener.security;

import com.jomea.urlshortener.entity.ApiKey;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.ApiKeyRepository;
import com.jomea.urlshortener.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Component
@Order(1)
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository, UserRepository userRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || path.startsWith("/api/auth/") || path.startsWith("/api/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("X-API-Key");
        if (authHeader != null && !authHeader.isBlank()) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                String hash = HexFormat.of().formatHex(md.digest(authHeader.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                ApiKey apiKey = apiKeyRepository.findByKeyHash(hash).orElse(null);
                if (apiKey != null) {
                    User user = userRepository.findById(apiKey.getUserId()).orElse(null);
                    if (user != null) {
                        apiKey.setLastUsedAt(LocalDateTime.now());
                        apiKeyRepository.save(apiKey);
                        var auth = new UsernamePasswordAuthenticationToken(
                            user.getEmail(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // Invalid API key — continue chain without authentication
            }
        }
        chain.doFilter(request, response);
    }
}
