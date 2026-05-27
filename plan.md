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

### Phase 5: Competitive Feature Roadmap

#### Core Link Features
18. Custom short codes / branded slugs (user-picked aliases)
19. Link editing (change destination after creation, keep short code)
20. Link expiry with optional auto-delete
21. Password-protected links
22. Bulk CSV/JSON link creation
23. UTM builder & campaign tagging presets

#### Analytics & Insights
24. Click analytics dashboard: time-series charts (hourly/daily/monthly)
25. Referrer breakdown (direct, social, search, etc.)
26. Device & browser breakdown
27. Geographic map of clicks (country/city level via GeoIP)
28. Export analytics to CSV/PDF

#### User System & Monetization
29. User accounts with registration (email + OAuth: Google, GitHub)
30. API key management for programmatic access (create per-user keys)
31. Tiered rate limits per API key
32. Paystack / Stripe payment integration for paid short links
33. Team workspaces with role-based access

#### Integrations & Automation
34. Webhooks for click events (POST to user's endpoint on every click)
35. Slack / Discord bot integration
36. Social media preview customization (custom OG tags per short link)
37. Browser extension (right-click → shorten current URL)
38. Zapier / Make.com / n8n integration

#### Technical & Infrastructure
39. Custom domain support (CNAME your own domain)
40. Redis caching for production (add back optionally)
41. PostgreSQL for production persistence (add back optionally)
42. Docker Compose with PostgreSQL + Redis for one-command deploy
43. Load balancing & horizontal scaling readiness
44. Rate limiting tiers (free: 100/h, pro: 10K/h, enterprise: unlimited)

#### Link Management
45. Link health monitoring (periodically check destination still alive)
46. QR code generation per short link (with download)
47. Link retargeting / pixels (Facebook, Google Ads)
48. Trash / soft-delete with restore
49. Tags & folders for organizing links
50. Search & filter (by code, destination, date, tags)
