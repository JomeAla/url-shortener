# URL Shortener - Task TODO

## Phase 1: Core Shortening
- [x] Initialize Spring Boot project with Java 21, Web, JPA, Validation, Actuator
- [x] Enable virtual threads in application.yml
- [x] Create Url JPA entity with unique index on shortCode
- [x] Create UrlRepository (findByShortCode)
- [x] Implement IdGenerator (snowflake → base62)
- [x] Create DTO records: ShortenRequest, ShortenResponse, StatsResponse
- [x] Add app.base-url and app.max-url-length config properties
- [x] Implement UrlService.shorten() and UrlService.resolve()
- [x] Write unit tests

## Phase 2: Caching
- [x] Implement in-memory CacheService (ConcurrentHashMap with TTL)
- [x] Wire cache-aside into UrlService

## Phase 3: Analytics & Redirect
- [x] Create ShortenController (POST /api/shorten, GET /api/urls, GET /api/stats/{code})
- [x] Create RedirectController (GET /{shortCode} → 302 redirect)
- [x] Implement async click counter (virtual thread, non-blocking)
- [x] Implement RateLimitingFilter (token bucket per IP, 100 req/min)
- [x] Test rate limiter with concurrent requests
- [x] Test full redirect flow end-to-end

## Phase 4: Web UI & Polish
- [x] Build Thymeleaf web UI with URL input, copy, and listing
- [x] Add global exception handler
- [x] Add Spring Boot Actuator health checks
- [x] Add CORS configuration
- [x] Fix base-url port mismatch (8080 → 8081)
- [x] Write README with setup, API docs, usage
- [x] Commit and push to GitHub

## Phase 5: Future Ideas
- [ ] QR code generation per short URL
- [ ] Admin dashboard with charts & analytics
- [ ] Paystack/telco payment integration for paid short links
- [ ] Redis / PostgreSQL for production deployment
- [ ] Custom short codes
- [ ] Link expiry and password protection
- [ ] Docker Compose deployment
