# URL Shortener

## Overview
A URL shortening service built with Java 21 virtual threads for high concurrency, H2 file-based persistence (with PostgreSQL option), and in-memory caching. Includes click tracking analytics, rate limiting, and a production web UI.

## Architecture

```
POST /api/shorten  ──►  Rate Limiter  ──►  UrlService.shorten(longUrl)
     (longUrl body)      (token bucket)       │
                                              ▼
                                         IdGenerator
                                     (snowflake→base62)
                                              │
                                              ▼
                                           H2 (save)
                                              │
                                              ▼
                                    In-Memory Cache (set)

GET /{shortCode}  ──►  UrlService.resolve(shortCode)  ──►  Cache (hit → 302)
                          │                                    │
                          ▼                                    ▼
                    RedirectController                    H2 (miss)
                          │                           (async click increment)
                          ▼
                     302 Redirect to longUrl
```

## Tech Stack
- Java 21 (virtual threads, records)
- Spring Boot 3.x + Spring Web, JPA, Validation, Actuator
- H2 (file-based dev) / PostgreSQL (production)
- In-memory cache (ConcurrentHashMap)
- Maven
- Thymeleaf (web UI)

## Implementation Plan

### Phase 1: Core Shortening
1. Initialize Spring Boot project (Java 21, virtual threads enabled)
2. Create Url entity and repository (H2, unique index on shortCode)
3. Implement IdGenerator (snowflake-style ID → base62 encoding)
4. Create DTO records: ShortenRequest (with @URL), ShortenResponse, StatsResponse
5. Create UrlService with shorten() + resolve()

### Phase 2: Caching & Performance
6. Implement in-memory CacheService with configurable TTL
7. Enable virtual threads

### Phase 3: Analytics & Redirect
8. Create ShortenController (POST /api/shorten)
9. Create RedirectController (GET /{shortCode} → 302)
10. Implement click tracking (async increment via virtual threads)
11. Add GET /api/stats/{shortCode} endpoint
12. Add rate limiting with token bucket filter

### Phase 4: Web UI & Polish
13. Build Thymeleaf web UI
14. Add GET /api/urls listing endpoint
15. Global exception handler
16. CORS configuration
17. README with setup and API docs

### Phase 5: Future Enhancements
18. QR code generation per short URL
19. Admin dashboard with charts
20. Paystack payment integration for paid short links
21. Redis for caching (optional production add-on)
22. Custom short codes
23. Link expiry and password protection
