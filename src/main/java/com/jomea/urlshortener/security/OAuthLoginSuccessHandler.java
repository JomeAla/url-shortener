package com.jomea.urlshortener.security;

import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuthLoginSuccessHandler.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    public OAuthLoginSuccessHandler(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId().toUpperCase();
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String providerId = getAttribute(attributes, "sub", "id");
        String email = getAttribute(attributes, "email");
        String name = getAttribute(attributes, "name");
        String avatarUrl = getAttribute(attributes, "picture", "avatar_url");

        if (email == null) {
            response.sendRedirect("/?oauth=error");
            return;
        }

        User user = userRepository.findByAuthProviderAndProviderId(provider, providerId).orElse(null);

        if (user == null) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setAuthProvider(provider);
                user.setProviderId(providerId);
                if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
            } else {
                boolean isFirst = userRepository.count() == 0;
                user = new User();
                user.setName(name != null ? name : email.split("@")[0]);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole(isFirst ? "ADMIN" : "USER");
                if (!isFirst) user.setTier("free");
                user.setAuthProvider(provider);
                user.setProviderId(providerId);
                if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
                user.setCreatedAt(LocalDateTime.now());
            }
            userRepository.save(user);
        }

        var springUser = new org.springframework.security.core.userdetails.User(
            user.getEmail(), user.getPassword(),
            java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            springUser, null, springUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        response.sendRedirect("/?oauth=success");
    }

    private String getAttribute(Map<String, Object> attrs, String... keys) {
        for (String key : keys) {
            Object val = attrs.get(key);
            if (val instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }
}