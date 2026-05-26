# URL Shortener

High-performance URL shortening service built with Java 21 virtual threads, Redis caching, PostgreSQL persistence, and token bucket rate limiting. Designed to handle 10K+ concurrent requests.

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

- **Java 21** with virtual threads
- **Spring Boot 3.4** (Web, JPA, Data Redis, Validation, Actuator)
- **PostgreSQL 17** (persistence, JPA/Hibernate)
- **Redis 7** (caching, cache-aside pattern, 24h TTL)

## Quick Start

Requires PostgreSQL and Redis running locally (or adjust `application.yml`).

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/url-shortener-1.0.0.jar
```

The service will be available at `http://localhost:8080`.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/shorten` | Shorten a URL |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/api/stats/{shortCode}` | Get click stats for a short URL |
| GET | `/actuator/health` | Health check |

## Example Usage

### Shorten a URL

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/url"}'
```

Response:
```json
{
  "shortUrl": "http://localhost:8080/abc123",
  "shortCode": "abc123",
  "longUrl": "https://example.com/very/long/url"
}
```

### Follow a redirect

```bash
curl -v http://localhost:8080/abc123
```

### Get stats

```bash
curl http://localhost:8080/api/stats/abc123
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
│   ├── ShortenController.java     # POST /api/shorten
│   └── RedirectController.java    # GET /{shortCode}
├── dto/
│   ├── ShortenRequest.java        # Request body record
│   ├── ShortenResponse.java       # Short URL response record
│   └── StatsResponse.java         # Stats response record
├── entity/
│   └── Url.java                   # JPA entity
├── exception/
│   └── GlobalExceptionHandler.java    # @RestControllerAdvice
├── repository/
│   └── UrlRepository.java         # Spring Data JPA
└── service/
    ├── CacheService.java          # Redis wrapper (24h TTL)
    ├── IdGenerator.java           # Snowflake-style → base62
    └── UrlService.java            # Shorten + resolve + stats
```

## Rate Limiting

- Token bucket algorithm, per-IP
- 100 requests per 60 seconds per IP
- Applies to all `/api/*` endpoints
- Returns HTTP 429 with JSON error when exceeded

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.base-url` | `http://localhost:8080` | Service domain for short URLs |
| `app.max-url-length` | `2048` | Maximum allowed URL length |

## Prerequisites

- Java 21+
- PostgreSQL 16+ (running on localhost:5432, database `urlshortener`, user `urlshortener`/`urlshortener`)
- Redis 7+ (running on localhost:6379)
