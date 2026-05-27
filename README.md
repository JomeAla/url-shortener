# URL Shortener

High-performance URL shortening service with click tracking, rate limiting, and a production web UI. Built with Java 21 virtual threads.

## Architecture

```
POST /api/shorten  ──►  Rate Limiter  ──►  UrlService.shorten(longUrl)
     (longUrl body)      (token bucket)       │
                                              ▼
                                         IdGenerator
                                     (snowflake→base62)
                                              │
                                              ▼
                                          H2/PostgreSQL (save)
                                              │
                                              ▼
                                    In-Memory Cache (set)

GET /{shortCode}  ──►  UrlService.resolve(shortCode)  ──►  Cache (hit → 302)
                          │                                     │
                          ▼                                     ▼
                    RedirectController                    H2/PostgreSQL (miss)
                          │                              (async click increment)
                          ▼
                     302 Redirect to longUrl
```

## Tech Stack

- **Java 21** with virtual threads
- **Spring Boot 3.4** (Web, JPA, Validation, Actuator)
- **H2** (file-based, zero-install) / **PostgreSQL** (production)
- **In-memory cache** (ConcurrentHashMap)
- **Maven**

## Quick Start

```bash
# Build
mvn clean package -DskipTests

# Run (no external dependencies needed)
java -jar target/url-shortener-1.0.0.jar
```

Open **http://localhost:8081** in your browser.

## Web UI

The web UI at `/` provides:
- Paste a URL and get a short link
- One-click copy to clipboard
- List of all shortened links with click counts
- Real-time updates (polls every 15s)

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/shorten` | Shorten a URL |
| GET | `/api/urls` | List all shortened URLs |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/api/stats/{shortCode}` | Get click stats |
| GET | `/actuator/health` | Health check |

## Example Usage

### Shorten a URL

```bash
curl -X POST http://localhost:8081/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url"}'
```

Response:
```json
{
  "shortUrl": "http://localhost:8081/abc123",
  "shortCode": "abc123",
  "longUrl": "https://example.com/very/long/url"
}
```

### Follow a redirect

```bash
curl -v http://localhost:8081/abc123
```

### Get stats

```bash
curl http://localhost:8081/api/stats/abc123
```

Response:
```json
{
  "shortCode": "abc123",
  "longUrl": "https://example.com/very/long/url",
  "clickCount": 42,
  "createdAt": "2026-05-26T23:00:00"
}
```

## Project Structure

```
src/main/java/com/jomea/urlshortener/
├── UrlShortenerApplication.java
├── config/
│   ├── AppProperties.java         # app.base-url, max-url-length
│   └── RateLimitingFilter.java    # Token bucket per-IP rate limiter
├── controller/
│   ├── ShortenController.java     # POST /api/shorten, GET /api/urls, GET /api/stats/{code}
│   └── RedirectController.java    # GET /{shortCode} → 302
├── dto/
│   ├── ShortenRequest.java        # Request body with @URL validation
│   ├── ShortenResponse.java       # Short URL response
│   └── StatsResponse.java         # Stats response
├── entity/
│   └── Url.java                   # JPA entity (id, longUrl, shortCode, createdAt, clickCount)
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   └── UrlRepository.java
└── service/
    ├── CacheService.java          # In-memory cache
    ├── IdGenerator.java           # Snowflake-style → base62
    └── UrlService.java            # Business logic
```

## Rate Limiting

- Token bucket algorithm, per-IP
- 100 requests per 60 seconds per IP
- Applies to all `/api/*` endpoints
- Returns HTTP 429 with JSON error when exceeded

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.base-url` | `http://localhost:8081` | Service domain for short URLs |
| `app.max-url-length` | `2048` | Maximum allowed URL length |
| `server.port` | `8081` | HTTP server port |

## Switching to PostgreSQL

Edit `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/urlshortener
    username: urlshortener
    password: urlshortener
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## Click Tracking

Each redirect visit asynchronously increments the click counter in a virtual thread with `@Transactional` isolation. No blocking on the redirect path.
