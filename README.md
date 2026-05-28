# Shrtly — URL Shortener

High-performance URL shortener with click analytics, user authentication, team workspaces, Slack/Discord bots, browser extension, and automation integrations. Built with Java 24 virtual threads, Spring Boot 3.4, PostgreSQL, and Redis.

## Features

- **URL Shortening** — Snowflake-style ID generation → base62, custom aliases, bulk import/CSV
- **Redirect & Tracking** — Async click counting, geo-IP lookup, browser/OS classification, referrer tracking
- **Analytics** — Per-link stats (clicks, browsers, countries, time series), public stats page
- **Authentication** — Email/password register/login, Google OAuth, GitHub OAuth
- **Admin Dashboard** — Manage users, view all links, promote/demote roles, system settings
- **User Dashboard** — Your links, click graphs, edit/delete links, API key management
- **Workspaces** — Multi-member workspaces with roles (owner, admin, editor, viewer)
- **Folders** — Organize links into folders per workspace
- **Tags** — Categorize links with tags, filter by tag
- **QR Codes** — Download QR code for any short link
- **Custom Domains** — Bring your own domain, DNS verification, SSL via Let's Encrypt
- **Link Customization** — OG tags (title, description, image), UTM builder, expiration dates, password protection
- **Rate Limiting** — Token bucket per-IP (100 req/60s), configurable, adjustable per user role
- **Trash** — Soft-deleted links, 30-day auto-purge, restore from trash
- **Webhooks** — Trigger on link click, customizable endpoints, retry with backoff, delivery logs
- **Slack Bot** — `/mylinks` (list recent links), `/analytics` (click stats with browser/country breakdown)
- **Discord Bot** — Slash commands for link management and analytics (requires manual bot token setup)
- **Browser Extension** — Chrome & Firefox MV3, right-click context menu, popup UI, settings page
- **API & Integrations** — REST API with API key auth, n8n/Make/Zapier integration guides
- **Subscription Plans** — Free (50 links), Pro (1000 links, all features), Enterprise (unlimited, SSO)
- **Payments** — Stripe checkout, Paystack (African markets), coupon/discount system
- **Promo Campaigns** — Banner announcements, coupon codes for checkout discounts
- **Link Blocker** — Block malicious/spam URLs by domain/pattern, block logs

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Java 24** with virtual threads |
| Framework | **Spring Boot 3.4** (Web, JPA, Validation, Actuator, Security) |
| Database | **PostgreSQL** (production), H2 (dev/test) |
| Cache | **Redis** (rate limits, session cache) |
| Build | **Maven** |
| Frontend | **Thymeleaf** + **Tailwind CSS** + **Chart.js** |
| Integrations | Slack Bolt SDK, Discord JDA, Stripe API, Paystack API |

## Quick Start

### Prerequisites

- Java 24+
- PostgreSQL (localhost:5432, database `urlshortener`)
- Redis (localhost:6379)
- Maven 3.9+

### Setup

```bash
# Clone
git clone https://github.com/JomeAla/url-shortener.git
cd url-shortener

# Configure database
# Edit src/main/resources/application.yml with your PostgreSQL credentials

# Set required env vars (or use defaults for local dev)
set GOOGLE_CLIENT_ID=dummy
set GOOGLE_CLIENT_SECRET=dummy
set GITHUB_CLIENT_ID=dummy
set GITHUB_CLIENT_SECRET=dummy

# Build
mvn clean package -DskipTests

# Run
java -jar target/url-shortener-1.0.0.jar
```

Open **http://localhost:8081** in your browser.

### Running with Redis

```bash
# Start Redis (Windows)
C:\tools\redis\redis-server.exe

# Start Redis (Linux/Mac)
redis-server
```

## API Endpoints

### Core
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/shorten` | API key | Shorten a URL |
| POST | `/api/shorten/bulk` | API key | Bulk shorten (CSV/JSON) |
| GET | `/{shortCode}` | — | Redirect to original URL |
| GET | `/api/urls` | Session/Key | List user's URLs |
| GET | `/api/urls/search?q=` | Session/Key | Search URLs |
| GET | `/api/stats/{shortCode}` | Session/Key | Click stats |
| GET | `/api/stats/{shortCode}/timeseries` | Session/Key | Daily click time series |
| DELETE | `/api/urls/{shortCode}` | Session/Key | Soft-delete a link |
| PUT | `/api/urls/{shortCode}` | Session/Key | Update link (alias, og, expiry) |

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns session |
| POST | `/api/auth/logout` | Logout |
| GET | `/oauth2/authorization/google` | Google OAuth login |
| GET | `/oauth2/authorization/github` | GitHub OAuth login |

### Admin
| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/dashboard` | Admin dashboard |
| GET | `/admin/users` | List all users |
| POST | `/admin/users/{id}/role` | Change user role |
| GET | `/admin/links` | List all links |
| DELETE | `/admin/links/{id}` | Force delete any link |
| GET | `/admin/settings` | System settings |

### Workspaces & Folders
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/workspaces` | Create workspace |
| GET | `/api/workspaces` | List user's workspaces |
| POST | `/api/workspaces/{id}/members` | Invite member |
| POST | `/api/folders` | Create folder |
| GET | `/api/folders` | List folders |

### Tags & QR
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tags` | Create tag |
| GET | `/api/tags` | List user's tags |
| GET | `/api/qr/{shortCode}` | Download QR code (PNG) |

### Custom Domains
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/domains` | Add custom domain |
| GET | `/api/domains` | List user's domains |
| POST | `/api/domains/{id}/verify` | Trigger DNS verification |

### Webhooks
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/webhooks` | Create webhook |
| GET | `/api/webhooks` | List webhooks |
| PUT | `/api/webhooks/{id}` | Update webhook |
| DELETE | `/api/webhooks/{id}` | Delete webhook |
| GET | `/api/webhooks/{id}/logs` | Webhook delivery logs |

### Subscriptions & Payments
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/plans` | List pricing plans |
| POST | `/api/subscriptions/create-checkout` | Stripe checkout session |
| POST | `/api/subscriptions/paystack/initialize` | Paystack transaction |
| GET | `/api/subscriptions/current` | Current user subscription |
| POST | `/api/coupons/validate` | Validate coupon code |

### Slack Bot
| Command | Description |
|---------|-------------|
| `/mylinks [count]` | List your recent shortened links |
| `/analytics <shortCode>` | Click stats with browser/country breakdown |

### Discord Bot
| Command | Description |
|---------|-------------|
| `/shorten <url> [alias]` | Shorten a URL |
| `/mylinks` | List your recent links |
| `/stats <code>` | Get click stats |

### Health
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Health check |

## Integrations

### n8n
1. Add **HTTP Request** node
2. Method: `POST` — URL: `https://yourdomain.com/api/shorten`
3. Headers: `Authorization: Bearer YOUR_API_KEY`
4. JSON Body: `{ "url": "{{ $json.url }}" }`

### Make (formerly Integromat)
1. Add **HTTP** module → **Make a request**
2. URL: `https://yourdomain.com/api/shorten`
3. Headers: `Authorization: Bearer YOUR_API_KEY`
4. Body: `{"url": "your-long-url"}`

### Zapier
- See `integrations/zapier/` for the CLI app definition
- Deploy with `zapier push`
- Triggers: New Link Click
- Actions: Create Short URL, Bulk Shorten
- Searches: List URLs, Get Analytics, Get Public Stats

## Browser Extension

- Chrome & Firefox (MV3 manifest)
- Right-click → "Shorten this link" / "Shorten this page"
- Popup: paste URL → shorten, view recent links
- Settings: configure base URL + API key
- See `browser-extension/README.md` for build instructions

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.base-url` | `http://localhost:8081` | Service domain for short URLs |
| `app.max-url-length` | `2048` | Maximum allowed URL length |
| `server.port` | `8081` | HTTP server port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/urlshortener` | Database URL |
| `spring.redis.host` | `localhost` | Redis host |
| `spring.redis.port` | `6379` | Redis port |

### Required Environment Variables

| Variable | Description |
|----------|-------------|
| `GOOGLE_CLIENT_ID` | Google OAuth client ID (set dummy for local) |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `GITHUB_CLIENT_ID` | GitHub OAuth client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth client secret |
| `DISCORD_BOT_TOKEN` | Discord bot token (for Discord bot feature) |

## Project Structure

```
src/main/java/com/jomea/urlshortener/
├── UrlShortenerApplication.java
├── config/
│   ├── AppProperties.java
│   ├── CacheConfig.java              # Redis cache configuration
│   ├── RateLimitingFilter.java       # Token bucket per-IP rate limiter
│   └── SecurityConfig.java           # OAuth, form login, API key auth
├── controller/
│   ├── ShortenController.java        # POST /api/shorten, GET /api/urls, stats
│   ├── RedirectController.java       # GET /{shortCode} → 302 redirect
│   ├── AuthController.java           # Register, login, logout
│   ├── AdminController.java          # Admin dashboard, user/role management
│   ├── WorkspaceController.java      # Workspace CRUD + members
│   ├── FolderController.java         # Folder CRUD
│   ├── TagController.java            # Tag CRUD
│   ├── WebhookController.java        # Webhook CRUD + logs
│   ├── CustomDomainController.java   # Custom domain CRUD + verify
│   ├── SlackController.java          # Slack bot event endpoints
│   ├── DiscordController.java        # Discord bot slash command endpoints
│   └── ...
├── dto/
│   ├── ShortenRequest.java
│   ├── ShortenResponse.java
│   ├── StatsResponse.java
│   └── ...
├── entity/
│   ├── Url.java
│   ├── User.java
│   ├── ClickEvent.java
│   ├── Webhook.java
│   ├── Workspace.java
│   └── ...
├── repository/
│   ├── UrlRepository.java
│   ├── UserRepository.java
│   ├── ClickEventRepository.java
│   └── ...
├── security/
│   └── OAuthLoginSuccessHandler.java
└── service/
    ├── UrlService.java
    ├── CacheService.java
    ├── IdGenerator.java
    ├── ClickEventService.java
    ├── UserService.java
    ├── WorkspaceService.java
    ├── FolderService.java
    ├── SlackBotService.java
    ├── DiscordBotService.java
    ├── WebhookService.java
    ├── CustomDomainService.java
    ├── GeoIpService.java
    ├── StripeService.java
    └── ...
```
