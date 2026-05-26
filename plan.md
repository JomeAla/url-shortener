# High-Performance URL Shortener

## Overview
A URL shortening service built with Java 21 virtual threads for high concurrency, Redis for caching, and PostgreSQL for persistence. Includes click tracking analytics and rate limiting. Designed to handle 10K+ concurrent requests.

## Architecture

```
POST /api/shorten  ──►  Rate Limiter  ──►  UrlService.shorten(longUrl)
     (longUrl body)      (token bucket)       │
                                              ▼
                                         IdGenerator
                                     (snowflake→base62)
                                              │
                                              ▼
                                        PostgreSQL (save)
                                              │
                                              ▼
                                        Redis Cache (set)

GET /{shortCode}  ──►  UrlService.resolve(shortCode)  ──►  Redis (cache hit → 302)
                          │                                    │
                          ▼                                    ▼
                    RedirectController                    PostgreSQL (miss → DB)
                          │                              (async click increment)
                          ▼
                     302 Redirect to longUrl
```

## Tech Stack
- Java 21 (virtual threads, records, pattern matching)
- Spring Boot 3.x + Spring Web
- PostgreSQL (JPA/Hibernate)
- Redis (Lettuce client, caching)
- Maven
- JMeter (load testing)

## Target Skills Demonstrated
- Modern Java 21 (virtual threads, records, sealed classes)
- High-concurrency API design
- Caching strategies (Redis, cache-aside pattern)
- Rate limiting (token bucket algorithm)
- REST API best practices
- Performance awareness

## Implementation Plan

### DTOs
- `ShortenRequest` — `{ "url": "..." }` with `@URL` validation
- `ShortenResponse` — `{ "shortUrl": "...", "shortCode": "...", "longUrl": "..." }`
- `StatsResponse` — `{ "shortCode": "...", "longUrl": "...", "clickCount": ..., "createdAt": ... }`

## Config
- `app.base-url` — service's own domain (e.g. `http://localhost:8080`)
- `app.max-url-length` — maximum allowed URL length (default 2048)
- Validation: reject blank/empty URLs, URLs exceeding max length, malformed URLs

## Phase 1: Core Shortening
1. Initialize Spring Boot project (Java 21, virtual threads enabled)
2. Create `Url` entity and repository (PostgreSQL, unique index on `shortCode`)
3. Implement `IdGenerator` (snowflake-style ID → base62 encoding)
4. Create DTO records: `ShortenRequest`, `ShortenResponse`, `StatsResponse`
5. Create `UrlService` with `shorten()` + `resolve(String shortCode)` logic

### Phase 2: Caching & Performance
5. Configure Redis (Lettuce client, connection pooling)
6. Implement cache-aside pattern in UrlService
7. Enable virtual threads (spring.threads.virtual.enabled=true)
8. Add rate limiting with token bucket filter

### Phase 3: Analytics & Redirect
9. Create ShortenController (POST /api/shorten)
10. Create RedirectController (GET /{shortCode} → 302)
11. Implement click tracking (async increment, no blocking)
12. Add GET /api/stats/{shortCode} endpoint

### Phase 4: Polish & Deploy
13. Global exception handler + CORS configuration
14. Load test with JMeter (10K concurrent requests)
15. Validation and error handling
16. README with performance benchmarks
