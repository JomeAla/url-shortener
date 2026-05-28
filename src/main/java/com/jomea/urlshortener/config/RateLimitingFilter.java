package com.jomea.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jomea.urlshortener.entity.Plan;
import com.jomea.urlshortener.entity.User;
import com.jomea.urlshortener.repository.PlanRepository;
import com.jomea.urlshortener.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    private static final int DEFAULT_MAX_REQUESTS = 60;
    private static final int ANONYMOUS_MAX_REQUESTS = 20;

    public RateLimitingFilter(UserRepository userRepository, PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        if (!path.startsWith("/api/") || path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        int maxRequests = resolveMaxRequests();

        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(maxRequests));

        if (bucket.tryConsume(maxRequests)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(),
                    Map.of("error", "Rate limit exceeded. Try again later."));
        }
    }

    private int resolveMaxRequests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ANONYMOUS_MAX_REQUESTS;
        }
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null || user.getTier() == null) return DEFAULT_MAX_REQUESTS;
        Plan plan = planRepository.findBySlug(user.getTier()).orElse(null);
        if (plan == null) return DEFAULT_MAX_REQUESTS;
        return plan.getMaxRequestsPerMinute();
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user_" + auth.getName();
        }
        return "ip_" + request.getRemoteAddr();
    }

    @Override
    public void init(jakarta.servlet.FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    static class Bucket {
        private int tokens;
        private long lastRefillNanos;
        final long refillPeriodNanos = 60_000_000_000L;

        Bucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume(int capacity) {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed > refillPeriodNanos) {
                long refills = elapsed / refillPeriodNanos;
                tokens = (int) Math.min(capacity, tokens + capacity * refills);
                lastRefillNanos = now;
            }
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }
    }
}