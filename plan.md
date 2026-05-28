# URL Shortener - Plan

## Current Status Summary

```
Phases 1-4 (Core): ✅ 100% Complete
Phase 5 (Features): ~25% Complete
Homepage Redesign:  Not started
```

## Legend
- ✅ = Fully implemented and tested
- 🔄 = Partially implemented (works but missing pieces)
- ❌ = Not implemented
- ⬜ = New item added in this plan

---

## Phase A: Homepage Redesign (TinyURL-Style)

Redesign index.html to match TinyURL's clean, modern aesthetic.

Design elements:
- Blue/teal gradient accents with decorative background shapes
- Bungee + Inter/Montserrat font pairing
- Centered hero with large URL input and "Shorten" CTA button
- Feature grid (Branded Links, QR Codes, Analytics, API) with icons
- Clean header: Logo | nav links | Sign in / Get Started buttons
- Responsive mobile layout
- Footer with links

### Tasks
- [ ] Rewrite index.html CSS with TinyURL-inspired color scheme
- [ ] Add decorative background blobs/shapes (SVG)
- [ ] Build hero section with prominent URL input
- [ ] Add feature grid section with icon cards
- [ ] Redesign header (logo left, auth/CTA right)
- [ ] Add footer
- [ ] Integrate auth modal, admin panel, QR modal, analytics modal into new layout
- [ ] Responsive/mobile optimization
- [ ] Test all features in new UI

---

## Phase B: Complete Partially Implemented Features

### B1. Link Management (UI)
- Status: 🔄 Backend supports list/create/edit/delete. UI shows list but no edit/delete buttons.
- [ ] Add edit button per link (inline edit or modal)
- [ ] Add delete button per link with confirmation
- [ ] Show QR code thumbnail inline
- [ ] Add link search/filter input on the list page

### B2. Click Analytics Dashboard (UI)
- Status: 🔄 Backend has `/api/analytics/{code}` with referrer/browser/device breakdown. UI shows a basic modal.
- [ ] Build full analytics dashboard page (not just modal)
- [ ] Add time-series chart (clicks over time - hourly/daily)
- [ ] Show referrer, browser, device breakdown as visual bars/charts
- [ ] Add click log table (recent clicks with timestamp, referrer, IP)
- [ ] Add CSV export button for analytics data

---

## Phase C: Missing Core Link Features

### C1. Bulk CSV/JSON Link Creation
- [ ] Add POST `/api/shorten/bulk` endpoint accepting array of URLs
- [ ] Accept CSV file upload or JSON array
- [ ] Return array of created links with any errors
- [ ] Add bulk import UI (file upload or paste list)

### C2. UTM Builder & Campaign Tagging Presets
- [ ] Add UTM parameter builder UI (source, medium, campaign, term, content)
- [ ] Auto-append UTM params to the long URL before shortening
- [ ] Save campaign metadata to DB (campaign name, UTM params)
- [ ] List/filter links by campaign

### C3. Link Search & Filter
- [ ] Add GET `/api/urls?q=search&tag=xxx&dateFrom=...&dateTo=...` endpoint
- [ ] Filter by short code, destination URL, date range
- [ ] Add search bar and filter controls to UI

### C4. Trash / Soft-Delete with Restore
- [ ] Add `deletedAt` timestamp field to Url entity (null = active)
- [ ] Change DELETE to soft-delete (set deletedAt, exclude from queries)
- [ ] Add POST `/api/urls/{shortCode}/restore` endpoint
- [ ] Add trash view in UI with restore option
- [ ] Add auto-purge for trashed links older than N days

### C5. Tags & Folders for Organizing Links
- [ ] Create Tag entity (id, name, userId)
- [ ] Create Folder entity (id, name, userId, parentFolderId)
- [ ] Add many-to-many relationship between Url and Tag
- [ ] Add folderId to Url entity
- [ ] Add CRUD endpoints for tags and folders
- [ ] Add tag/folder management UI

---

## Phase D: Missing Analytics & Insights Features

### D1. Geographic Map of Clicks (GeoIP)
- [ ] Integrate GeoIP lookup (MaxMind GeoLite2 or ip2location)
- [ ] Parse IP from ClickEvent and resolve country/city on save
- [ ] Add country and city fields to ClickEvent
- [ ] Add `/api/analytics/{code}/geo` endpoint returning country/city breakdown
- [ ] Add map visualization in analytics UI

### D2. Export Analytics to CSV/PDF
- [ ] Add GET `/api/analytics/{code}/export/csv` endpoint
- [ ] Add GET `/api/analytics/{code}/export/pdf` endpoint
- [ ] Add export buttons in analytics UI

---

## Phase E: Missing User System & Monetization Features

### E1. OAuth Login (Google, GitHub)
- [ ] Add spring-boot-starter-oauth2-client dependency
- [ ] Configure Google OAuth provider
- [ ] Configure GitHub OAuth provider
- [ ] Link OAuth accounts to existing User entity
- [ ] Add "Sign in with Google/GitHub" buttons to auth modal

### E2. API Key Management
- [ ] Create ApiKey entity (id, key, userId, name, permissions, expiresAt)
- [ ] Add API key generation endpoint
- [ ] Add API key authentication filter
- [ ] Add API key management UI (create, revoke, list)

### E3. Tiered Rate Limits
- [ ] Add rate limit tier config (free: 100/h, pro: 10K/h, enterprise: unlimited)
- [ ] Assign tier to User entity
- [ ] Update RateLimitingFilter to check user tier
- [ ] Check API key tier when using key-based auth

### E4. Paystack / Stripe Payment Integration
- [ ] Add payment provider abstraction (interface)
- [ ] Implement Paystack integration
- [ ] Implement Stripe integration
- [ ] Create pricing table UI
- [ ] Add webhook handlers for payment events
- [ ] Upgrade user tier on successful payment

### E5. Team Workspaces
- [ ] Create Workspace entity (id, name, ownerId)
- [ ] Create WorkspaceMember entity (workspaceId, userId, role)
- [ ] Add workspaceId to Url entity (optional)
- [ ] Add workspace CRUD and member management endpoints
- [ ] Add workspace switcher UI

---

## Phase F: Missing Admin Payment Settings

### F1. Payment Gateway Admin UI
- [ ] Create PaymentConfig entity (activeProvider, sandboxMode, encrypted credentials)
- [ ] Build admin payment settings page (toggle Paystack/Stripe)
- [ ] Add sandbox/live mode toggle
- [ ] Add encrypted credential storage (AES-256)

---

## Phase G: Missing Integrations & Automation

### Implementation Priority (Recommended Order)
```
G0 (API Docs) → G1 (Webhooks) → G4 (n8n/Zapier) → G2 (Discord) → G3 (Slack) → G5 (OG Tags) → G6 (Browser Ext)
```
Webhooks (G1) are the foundation — n8n, Zapier, and Make all consume webhooks. Discord (G2) is lighter than Slack (G3) since it needs no OAuth complexity.

### G0. Pre-requisite: Public API Documentation
- [ ] Document all existing REST API endpoints with examples
- [ ] Publish API docs at /docs or a dedicated page
- [ ] Add API base URL and auth instructions (X-API-Key header)

### G1. Webhooks (Foundation for all integrations)
- [ ] Create Webhook entity (id, userId, url, events[], secret, active, createdAt)
- [ ] Add WebhookRepository
- [ ] POST /api/webhooks — create webhook
- [ ] GET /api/webhooks — list user's webhooks
- [ ] PUT /api/webhooks/{id} — update webhook
- [ ] DELETE /api/webhooks/{id} — delete webhook
- [ ] POST /api/webhooks/{id}/test — send test payload
- [ ] Fire webhook on click event (async via @Async or task executor)
- [ ] Implement retry with exponential backoff (3 attempts)
- [ ] Sign webhook payload with HMAC-SHA256 (X-Webhook-Signature header)
- [ ] Add webhook management UI in dashboard settings
- [ ] Add webhook logs table (delivery status, response code, timestamp)

### G2. Discord Bot
- [ ] Add Discord bot dependency (JDA or Discord4J)
- [ ] Register slash command: /shorten <url> [custom_code] [password]
- [ ] Handle ephemeral response with short URL + stats
- [ ] Add /mylinks command to list recent links
- [ ] Add /analytics <code> command to show click stats
- [ ] Bot token configuration in admin settings
- [ ] Docker-compose bot worker

### G3. Slack Bot
- [ ] Add Slack SDK dependency (bolt)
- [ ] Register slash command: /shorten <url>
- [ ] Handle ephemeral response with short URL
- [ ] Add shortcut action from message context menu
- [ ] OAuth install flow (Add to Slack button)
- [ ] Bot token + signing secret in admin settings
- [ ] Docker-compose bot worker

### G4. n8n / Make / Zapier Integration
- [ ] Create webhook-triggered "Link Clicked" event (uses G1 webhooks)
- [ ] Create "URL Shortened" action (Zapier app action / n8n node)
- [ ] Provide webhook URL format: POST /api/webhooks/receive/{provider}
- [ ] Add Zapier app definition (public API + webhook triggers)
- [ ] Add platform-specific docs for connecting n8n/Make/Zapier
- [ ] Rate-limited public API tier (free: 100/h, pro: 10k/h, ent: unlimited)

### G5. Social Media Preview Customization (OG Tags)
- [ ] Add og:title, og:description, og:image fields to Url entity
- [ ] Serve OG meta tags on `/{shortCode}` for social crawlers
- [ ] Add OG preview editor UI when creating/editing links

### G6. Browser Extension
- [ ] Chrome manifest V3 (manifest.json)
- [ ] Firefox manifest V3 (manifest.json)
- [ ] Right-click context menu → "Shorten with Shrtly"
- [ ] Popup UI showing recent links + shorten input
- [ ] API key auth from extension settings
- [ ] Publish to Chrome Web Store + Firefox Add-ons

---

## Phase H: Missing Technical & Infrastructure Features

### H1. Custom Domain Support (CNAME)
- [ ] Create CustomDomain entity (id, userId, domain, verified, sslStatus)
- [ ] Add DNS verification endpoint (TXT record check)
- [ ] Dynamically serve short links on custom domain
- [ ] Auto-provision SSL (Let's Encrypt via ACME)
- [ ] Add custom domain management UI

### H2. Redis Caching
- [ ] Add spring-boot-starter-data-redis dependency
- [ ] Implement RedisCacheService as alternative to in-memory
- [ ] Make cache provider configurable via application.yml

### H3. PostgreSQL + Docker Compose
- [ ] Create docker-compose.yml with PostgreSQL + Redis + app
- [ ] Create Dockerfile for the app (multi-stage build)
- [ ] Add health checks to compose file
- [ ] Make PostgreSQL the default for Docker deployment

### H4. Rate Limiting Tiers
- [ ] (Covered in E3 - will be implemented together)

---

## Implementation Order (Recommended)

```
Phase A (Homepage Redesign) → Highest priority, user-facing impact
Phase B (Complete Partial Features) → High priority, closes gaps
Phase C1-C3 (Bulk, UTM, Search) → Medium, practical value
Phase C4-C5 (Trash, Tags) → Medium, power-user features
Phase D1-D2 (GeoIP, Export) → Medium, analytics depth
Phase H2-H3 (Redis, Docker) → Medium, infrastructure
Phase E1-E5 (Users/Monetization) → Lower, business features
Phase F1 (Payment Settings) → Lower, depends on E4
Phase G1-G5 (Integrations) → Lowest, ecosystem plays
```

Note: Dependencies flow downward. E.g., F1 depends on E4 being started.
