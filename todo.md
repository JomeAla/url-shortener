# URL Shortener - Task TODO

## Legend
- [x] = Done
- [-] = Partially done
- [ ] = Not started

---

## Phase A: Homepage & Branding
- [x] Premium high-end redesign (navy/gold/ivory palette, warm ivory bg, deep navy text, gold accents)
- [x] Hero section with enlarged badge, 3.5rem gradient title, "Built for Africa" branding
- [x] "Everything you get" 2-column feature grid with all features
- [x] Webhook management tab, CRUD modal, delivery log viewer
- [x] Pricing section with gold featured badge, gold glow
- [x] Auth/admin/QR/analytics modals integrated
- [x] Responsive/mobile optimization

---

## Phase B: Link & Analytics UI

### B1. Link Management UI
- [x] Backend: POST/GET/PUT/DELETE `/api/urls` all work
- [x] Edit button + modal per link in UI
- [x] Delete button with confirmation in UI

### B2. Analytics Dashboard
- [x] Backend: `/api/analytics/{code}` returns referrer/browser/device
- [x] Time-series chart rendered in analytics view
- [x] Referrer + Device breakdown bars rendered
- [x] Browser breakdown bars rendered
- [x] CSV export button works
- [x] Link selector + interval picker in UI

---

## Phase C: Core Link Features

### C1. Bulk Import
- [x] Backend: POST `/api/shorten/bulk` exists
- [x] Bulk import UI with JSON paste + CSV upload, preview table, and results display

### C2. UTM Builder
- [x] Backend: UTM fields on Url entity, accepted in POST/PUT
- [x] UTM builder UI in create form (hero) and edit modal
- [x] Auto-append UTM params to destination URL on redirect

### C3. Link Search & Filter
- [x] Backend: GET `/api/urls?q=&dateFrom=&dateTo=` works
- [x] Search input with real-time filtering in UI
- [x] Date range filter UI (from/to date inputs)
- [x] Tag filter (text input, case-insensitive contains match on comma-separated tags)

### C4. Trash / Soft-Delete
- [x] `deletedAt` field on Url entity
- [x] Soft-delete (DELETE /api/urls/{code}), restore (POST /api/urls/{code}/restore), list trash (GET /api/urls/trash), permanent delete (DELETE /api/urls/trash/{code})
- [x] Scheduled auto-purge (daily at 3AM, removes items >30 days)
- [x] Trash view UI with restore and permanent delete buttons

### C5. Tags & Folders
- [x] Folder entity + Tag entity + repositories
- [x] Folder CRUD (POST/GET/PUT/DELETE /api/folders)
- [x] Tag CRUD (POST/GET/PUT/DELETE /api/tags)
- [x] folderId on Url entity, accepted in create/update
- [x] Filter by folderId in GET /api/urls
- [x] Folder management modal (create, rename, delete with color picker)
- [x] Tag management modal (create, rename, delete with color picker)
- [x] Folder selector in create form (advanced options) and edit modal
- [x] Tag badges displayed on link items in the list
- [x] "manage" links next to folder filter and tag inputs

---

## Phase D: Analytics & Insights

### D1. GeoIP Map
- [-] MaxMind GeoLite2 integration (using ip-api.com instead — free, no DB needed)
- [x] Country/city on ClickEvent
- [x] `/api/analytics/{code}/geo` endpoint
- [x] Map visualization in UI (Leaflet + country breakdown bars)

### D2. Export CSV (Analytics)
- [x] CSV export endpoint works
- [x] Export button in analytics toolbar

---

## Phase E: User System & Monetization

### E1. OAuth Login
- [x] spring-boot-starter-oauth2-client
- [x] Google OAuth provider config (client-id/secret via GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET env vars)
- [x] GitHub OAuth provider config (client-id/secret via GITHUB_CLIENT_ID/GITHUB_CLIENT_SECRET env vars)
- [x] Link OAuth to existing User entity (auto-links if email matches, or creates new user)
- [x] Social login buttons in UI (Google + GitHub with SVG icons in auth modal)
- [x] OAuth callback handler (auto-detects `?oauth=success` on redirect back)

### E2. API Key Management
- [x] ApiKey entity + CRUD endpoints
- [x] API key auth filter (X-API-Key header)
- [x] API key management UI in dashboard

### E3. Tiered Rate Limits
- [x] maxRequestsPerMinute field on Plan entity (Free=30, Pro=200, Enterprise=1000)
- [x] RateLimitingFilter resolves authenticated user, looks up their plan's rate limit
- [x] Unauthenticated users get 20 req/min, unknown tier users get 60 req/min
- [x] Buckets keyed by user email (auth) or IP (anonymous), runs at @Order(2) after ApiKeyAuthFilter

### E4. Subscriptions & Payments
- [x] SubscriptionController with initialize/verify/validate-coupon
- [x] Pricing table in UI with featured gold plan
- [x] Paystack payment verification flow
- [x] Stripe integration (alternative provider)

### E5. Team Workspaces
- [x] Workspace entity + members
- [x] workspaceId on Url
- [x] CRUD endpoints
- [x] Workspace switcher UI

---

## Phase F: Admin

- [x] Admin stats, users, plans, banners, coupons, settings CRUD
- [x] Promo banner management (list, create, update, delete)
- [x] Coupon management (list, create, update, delete)
- [x] Admin settings (payment, SMTP, branding) with logo upload
- [x] Sandbox/live toggle for payment gateway (test/live key pairs, services read sandboxMode)
- [x] AES-256 encrypted credential storage (AES/GCM/NoPadding, 256-bit key via SHA-256)

---

## Phase G: Integrations & Automation

### G0. Public API Documentation
- [x] All REST API endpoints documented at `/docs`
- [x] Auth instructions (session + X-API-Key header)

### G1. Webhooks
- [x] Webhook entity + repository
- [x] Full CRUD endpoints (POST/GET/PUT/DELETE + test)
- [x] Async dispatch on `link.clicked` event
- [x] Exponential backoff retry (3 attempts)
- [x] HMAC-SHA256 payload signing (X-Webhook-Signature)
- [x] Webhook management UI in dashboard
- [x] Webhook logs viewer with detail modal

### G2. Discord Bot
- [x] JDA 5.3.0 dependency added
- [x] DiscordBotService with JDA lifecycle (start/stop/auto-start)
- [x] `/link` slash command (email+password linking)
- [x] `/shorten` slash command (URL shortening)
- [x] `/mylinks` slash command (recent links)
- [x] `/analytics` slash command (click stats, browser, country)
- [x] Bot token config in admin settings (encrypted via AesEncryption)
- [x] Bot controls UI in admin panel (start/stop/status)
- [x] Discord linking status UI in dashboard settings
- [-] **TODO: Set bot token** — Log into admin panel → Settings → Discord Bot → enter token, then click Start. Token is encrypted and stored in DB. *(Requires user to create a Discord app, generate a bot token at https://discord.com/developers/applications, and enter it in the admin panel.)*

### G3. Slack Bot
- [x] Bolt SDK dependency
- [x] `/shorten` slash command
- [x] `/link` slash command (email+password linking)
- [x] `/mylinks` slash command (recent links)
- [x] `/analytics` slash command (click stats, browser, country)
- [x] Bot token + app token + signing secret in admin settings (encrypted via AesEncryption)
- [x] Bot controls UI in admin panel (start/stop/status)
- [x] Slack linking status UI in dashboard settings

### G4. n8n / Make / Zapier
- [x] Webhook-triggered "Link Clicked" event (uses G1 webhooks) — already functional
- [x] "Shorten URL" action (uses existing REST API) — already functional
- [x] Zapier app definition (triggers + actions + searches) at `integrations/zapier/`
- [x] Platform-specific connection docs in `/docs` page (n8n, Make, Zapier sections)
- [x] Integration README at `integrations/README.md`

### G5. Social Preview OG Tags
- [x] og:title, og:description, og:image on Url entity
- [x] OG meta tags on `/{shortCode}` for crawlers (serves HTML page with og:meta + refresh redirect for Twitterbot, Facebook, Slack, WhatsApp, Telegram, Discord, etc.)
- [x] OG preview editor in link create/edit UI

### G6. Browser Extension
- [x] Chrome manifest V3 (compatible with Firefox via `browser_specific_settings`)
- [x] Right-click context menu → "Shorten with Shrtly" (link + page contexts)
- [x] Popup UI with shorten input + recent links list (clipboard copy on click)
- [x] Options/settings page (base URL + API key config + test connection)
- [x] Notification on successful shorten (auto-copies to clipboard)
- [x] Icons: SVG source + generated PNGs (16, 48, 128) via `sharp`
- [x] Build & packaging docs in `browser-extension/README.md`

---

## Phase H: Technical & Infrastructure

### H1. Custom Domains
- [x] CustomDomain entity + DNS verification (TXT record lookup via javax.naming)
- [x] Serve links on custom domain (domain-aware redirect in RedirectController)
- [x] Custom Domain management UI in dashboard (Domains tab, add/verify/remove)
- [x] Plan enforcement (gated behind pro/enterprise)
- [ ] Auto-SSL (Let's Encrypt) — handled externally by reverse proxy (Caddy/Traefik)

### H2. Redis Caching
- [x] Redis dependency already in pom.xml
- [x] CacheService backed by StringRedisTemplate (Redis) + ConcurrentHashMap (L1 hot cache)
- [x] 24-hour TTL on cached entries
- [x] RedisAutoConfiguration re-enabled (was excluded)
- [x] CacheConfig with StringRedisTemplate bean

### H3. Docker & PostgreSQL
- [x] PostgreSQL config in application.yml (env-var driven: DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD)
- [x] HikariCP connection pool configured (10 max, 2 min idle)
- [x] H2 moved to test scope
- [ ] Dockerfile (multi-stage build)
- [ ] docker-compose.yml (app + PostgreSQL + Redis)
- [ ] Health checks

