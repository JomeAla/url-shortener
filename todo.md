# URL Shortener - Task TODO

## Legend
- [x] = Done
- [-] = Partially done
- [ ] = Not started

---

## Phase A: Homepage Redesign (TinyURL-Style)

- [ ] Rewrite index.html with TinyURL-inspired CSS (blue/teal accents, Bungee+Inter fonts)
- [ ] Add decorative SVG background blobs
- [ ] Build hero section with prominent centered URL input + "Shorten" CTA
- [ ] Add feature grid: Branded Links, QR Codes, Analytics, API
- [ ] Redesign header: logo left, nav links, Sign in / Get Started buttons
- [ ] Add footer with links
- [ ] Integrate auth/admin/QR/analytics modals into new layout
- [ ] Responsive/mobile optimization
- [ ] Test all endpoints in new UI

---

## Phase B: Complete Partially Implemented Features

### B1. Link Management UI
- [-] Backend: POST/GET/PUT/DELETE `/api/urls` all work
- [ ] Add edit button + modal per link in UI
- [ ] Add delete button with confirmation in UI

### B2. Click Analytics Dashboard
- [-] Backend: `/api/analytics/{code}` returns referrer/browser/device
- [ ] Build full analytics dashboard page with time-series chart
- [ ] Add visual breakdown bars for referrer/browser/device
- [ ] Add CSV export for analytics data

---

## Phase C: Missing Core Link Features

### C1. Bulk CSV/JSON Creation
- [ ] POST `/api/shorten/bulk` endpoint
- [ ] CSV file upload / JSON array support
- [ ] Bulk import UI

### C2. UTM Builder
- [ ] UTM parameter presets UI
- [ ] Auto-append UTM to long URL
- [ ] Campaign metadata in DB

### C3. Link Search & Filter
- [ ] GET `/api/urls?q=&tag=&dateFrom=&dateTo=`
- [ ] Search/filter UI controls

### C4. Trash / Soft-Delete
- [ ] `deletedAt` field on Url entity
- [ ] Soft-delete + restore endpoints
- [ ] Trash view UI with auto-purge

### C5. Tags & Folders
- [ ] Tag entity + folder entity
- [ ] CRUD endpoints
- [ ] Tag/folder management UI

---

## Phase D: Missing Analytics & Insights

### D1. GeoIP Map
- [ ] MaxMind GeoLite2 integration
- [ ] Country/city on ClickEvent
- [ ] `/api/analytics/{code}/geo` endpoint
- [ ] Map visualization in UI

### D2. Export CSV/PDF
- [ ] CSV export endpoint
- [ ] PDF export endpoint
- [ ] Export buttons in UI

---

## Phase E: User System & Monetization

### E1. OAuth Login
- [ ] spring-boot-starter-oauth2-client
- [ ] Google OAuth provider config
- [ ] GitHub OAuth provider config
- [ ] Link OAuth to existing User entity
- [ ] Social login buttons in UI

### E2. API Key Management
- [ ] ApiKey entity + CRUD
- [ ] API key auth filter
- [ ] API key management UI

### E3. Tiered Rate Limits
- [ ] Tier config (free/pro/enterprise)
- [ ] Tier on User entity
- [ ] Update RateLimitingFilter

### E4. Paystack / Stripe
- [ ] Payment provider abstraction
- [ ] Paystack integration
- [ ] Stripe integration
- [ ] Pricing table UI
- [ ] Payment webhook handlers
- [ ] Upgrade user tier on payment

### E5. Team Workspaces
- [ ] Workspace entity + members
- [ ] workspaceId on Url
- [ ] CRUD endpoints
- [ ] Workspace switcher UI

---

## Phase F: Admin Payment Settings

- [ ] PaymentConfig entity
- [ ] Admin payment settings page
- [ ] Sandbox/live toggle
- [ ] AES-256 encrypted credential storage

---

## Phase G: Integrations & Automation

### G1. Webhooks
- [ ] Webhook entity + CRUD
- [ ] Fire on click event
- [ ] HMAC-SHA256 signing
- [ ] Webhook management UI

### G2. Slack / Discord Bot
- [ ] Slack command handler
- [ ] Discord command handler
- [ ] OAuth install flow

### G3. Social Preview (OG Tags)
- [ ] og:title, og:description, og:image on Url
- [ ] Serve OG meta on `/{shortCode}`
- [ ] OG editor UI

### G4. Browser Extension
- [ ] Chrome manifest
- [ ] Firefox manifest
- [ ] Right-click → shorten
- [ ] Popup UI

### G5. Zapier / Make / n8n
- [ ] Public API docs
- [ ] Zapier app definition
- [ ] Publish to ecosystem

---

## Phase H: Technical & Infrastructure

### H1. Custom Domains
- [ ] CustomDomain entity + DNS verification
- [ ] Serve links on custom domain
- [ ] Auto-SSL (Let's Encrypt)
- [ ] Custom domain UI

### H2. Redis Caching
- [ ] Redis dependency
- [ ] RedisCacheService implementation
- [ ] Configurable cache provider

### H3. PostgreSQL + Docker
- [ ] Dockerfile (multi-stage build)
- [ ] docker-compose.yml (app + PostgreSQL + Redis)
- [ ] Health checks

### H4. Rate Limiting Tiers
- [ ] (Covered in E3)
