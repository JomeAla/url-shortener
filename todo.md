# URL Shortener - Task TODO

## Phase 1: Core Shortening
- [x] Initialize Spring Boot project with Java 21, Web, JPA, Redis starters
- [x] Enable virtual threads in application.yml (spring.threads.virtual.enabled=true)
- [x] Create `Url` JPA entity (id, longUrl, shortCode, createdAt, clickCount) with unique index on shortCode
- [x] Create `UrlRepository` (findByShortCode)
- [x] Implement `IdGenerator` (snowflake-style ID → base62 encoding)
- [x] Create DTO records: `ShortenRequest` (with @URL validation), `ShortenResponse`, `StatsResponse`
- [x] Add `app.base-url` and `app.max-url-length` config properties
- [x] Implement `UrlService.shorten(longUrl)` with URL validation + `UrlService.resolve(shortCode)`
- [x] Write unit test for ID generation, collision handling, and URL validation

## Phase 2: Caching & Performance
- [x] Configure Redis connection (Lettuce client, pool settings)
- [x] Implement `CacheService` (get/put/invalidate with TTL)
- [x] Wire cache-aside into `UrlService` (cache hit → return, miss → DB → cache)
- [x] Add virtual thread config in application.yml (spring.threads.virtual.enabled=true)

## Phase 3: Analytics & Redirect
- [x] Create `ShortenController` (POST /api/shorten with ShortenRequest body)
- [x] Create `RedirectController` (GET /{shortCode} → 302 redirect)
- [x] Implement async click counter (virtual thread, non-blocking increment)
- [x] Add `GET /api/stats/{shortCode}` endpoint (click count, createdAt, longUrl)
- [x] Implement `RateLimitingFilter` (token bucket per IP, 100 req/min)
- [ ] Test rate limiter with concurrent requests
- [ ] Test full redirect flow end-to-end

## Phase 4: Polish & Deploy
- [x] Add global exception handler (@ControllerAdvice)
- [x] Add Spring Boot Actuator health checks
- [ ] Add CORS configuration
- [ ] Create JMeter test plan (10K concurrent requests)
- [ ] Run load test and document results (p50/p95/p99 latency)
- [ ] Write README with setup, API docs, and benchmark results
