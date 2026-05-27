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

## Phase 5: Competitive Feature Roadmap

### Core Link Features
- [ ] Custom short codes / branded slugs
- [ ] Link editing (change destination after creation)
- [ ] Link expiry with optional auto-delete
- [ ] Password-protected links
- [ ] Bulk CSV/JSON link creation
- [ ] UTM builder & campaign tagging presets

### Analytics & Insights
- [ ] Click analytics dashboard (time-series charts)
- [ ] Referrer breakdown (direct, social, search)
- [ ] Device & browser breakdown
- [ ] Geographic map of clicks (GeoIP)
- [ ] Export analytics to CSV/PDF

### User System & Monetization
- [ ] User accounts (email + OAuth: Google, GitHub)
- [ ] API key management per user
- [ ] Tiered rate limits per API key
- [ ] Paystack / Stripe payment integration for paid links
- [ ] Team workspaces with role-based access

### Admin Payment Settings
- [ ] Dashboard payment configuration page (switch between Paystack & Stripe)
- [ ] Sandbox / live mode toggle for testing payments
- [ ] Encrypted credential storage in database (AES-256)

### Integrations & Automation
- [ ] Webhooks for click events
- [ ] Slack / Discord bot integration
- [ ] Social media preview customization (OG tags)
- [ ] Browser extension (right-click → shorten)
- [ ] Zapier / Make / n8n integration

### Technical & Infrastructure
- [ ] Custom domain support (CNAME)
- [ ] Redis caching for production
- [ ] PostgreSQL for production persistence
- [ ] Docker Compose with PostgreSQL + Redis
- [ ] Load balancing & horizontal scaling
- [ ] Rate limiting tiers (free / pro / enterprise)

### Link Management
- [ ] Link health monitoring (uptime checks)
- [ ] QR code generation per link (with download)
- [ ] Link retargeting / pixels (Facebook, Google Ads)
- [ ] Trash / soft-delete with restore
- [ ] Tags & folders for organizing links
- [ ] Search & filter (code, destination, date, tags)
