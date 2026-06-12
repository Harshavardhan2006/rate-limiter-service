# Rate Limiter as a Service

A distributed rate limiting service built with Java, Spring Boot, and Redis. Supports two algorithms — Token Bucket and Sliding Window — selectable per client via a clean REST API.

## Tech Stack
- Java 21
- Spring Boot 3.5.x
- Redis (via Docker)
- Maven

## Algorithms Implemented
**Token Bucket** — Each client gets N tokens per window. Tokens refill when the window expires. Allows burst traffic up to the bucket size.

**Sliding Window** — Stores request timestamps in a Redis sorted set. Removes timestamps outside the window on every request. More precise than Token Bucket but uses more memory per client.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/ratelimiter/register | Register a client with rate limit config |
| GET | /api/ratelimiter/check/{clientId} | Check if request is allowed |
| GET | /api/ratelimiter/stats/{clientId} | Get client stats |
| GET | /api/ratelimiter/admin/rules | Get all registered clients |
| PUT | /api/ratelimiter/admin/rules/{clientId} | Update client rules |
| DELETE | /api/ratelimiter/admin/rules/{clientId} | Remove a client |

## Running Locally

**Start Redis:**
```bash
docker run -d -p 6379:6379 --name redis-ratelimiter redis
```

**Run the app:**
```bash
mvn spring-boot:run
```

**Register a client:**
```bash
curl -X POST http://localhost:8080/api/ratelimiter/register \
-H "Content-Type: application/json" \
-d '{"clientId":"test","maxRequests":5,"windowSizeInSeconds":60,"strategy":"tokenBucket"}'
```

## Design Decisions
- **Strategy Pattern** — algorithms are swappable per client without changing API or service layer
- **Redis atomic operations** — `DECR` and sorted set operations prevent race conditions under concurrent load
- **TTL-based cleanup** — Redis keys auto-expire after the window, no manual cleanup needed