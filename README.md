# Rate Limiter as a Service

A distributed rate limiting service built with Java, Spring Boot, and Redis. Supports two algorithms — Token Bucket and Sliding Window — selectable per client via a clean REST API.

## Live Demo

| | URL |
|---|---|
| API Base | https://rate-limiter-service.up.railway.app |
| Dashboard | https://rate-limiter-service.up.railway.app/dashboard.html |

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Redis (via Docker locally, Railway in production)
- Maven

## Architecture

```
Client Request
      ↓
RateLimiterController   (HTTP layer)
      ↓
RateLimiterService      (business logic)
      ↓
RateLimitStrategy       (interface)
   ↙        ↘
TokenBucket  SlidingWindow
      ↓
    Redis     (distributed state)
```

The Strategy Pattern makes algorithms swappable per client — changing the algorithm for a client requires no code changes, only a config update via the API.

## Algorithms

### Token Bucket
Each client gets a bucket with N tokens. Every request consumes one token. When the time window expires, the bucket refills to N. If the bucket is empty — the request is blocked.

- Storage: O(1) per client (token count + last refill timestamp)
- Allows burst traffic up to the bucket size
- Best for: APIs where short bursts are acceptable

### Sliding Window
Every request timestamp is stored in a Redis sorted set. On each new request, timestamps older than the window are removed. If the remaining count is at the limit — the request is blocked.

- Storage: O(n) per client where n = requests in the current window
- More precise — no burst allowance near window boundaries
- Best for: APIs requiring strict per-window enforcement

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ratelimiter/register` | Register a client with rate limit config |
| GET | `/api/ratelimiter/check/{clientId}` | Check if a request is allowed |
| GET | `/api/ratelimiter/stats/{clientId}` | Get stats for a client |
| GET | `/api/ratelimiter/admin/rules` | Get all registered clients |
| PUT | `/api/ratelimiter/admin/rules/{clientId}` | Update a client's rules |
| DELETE | `/api/ratelimiter/admin/rules/{clientId}` | Remove a client |

## Request & Response Examples

### Register a client
```bash
curl -X POST https://rate-limiter-service.up.railway.app/api/ratelimiter/register \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "harsha",
    "maxRequests": 5,
    "windowSizeInSeconds": 60,
    "strategy": "tokenBucket"
  }'
```
Response:
```
Client harsha registered successfully
```

### Check rate limit
```bash
curl https://rate-limiter-service.up.railway.app/api/ratelimiter/check/harsha
```
Response (allowed):
```json
{
  "allowed": true,
  "clientId": "harsha",
  "message": "Request allowed",
  "retryAfterSeconds": 0
}
```
Response (blocked):
```json
{
  "allowed": false,
  "clientId": "harsha",
  "message": "Rate limit exceeded",
  "retryAfterSeconds": 60
}
```

### Update rules at runtime
```bash
curl -X PUT https://rate-limiter-service.up.railway.app/api/ratelimiter/admin/rules/harsha \
  -H "Content-Type: application/json" \
  -d '{
    "maxRequests": 10,
    "windowSizeInSeconds": 30,
    "strategy": "slidingWindow"
  }'
```

## Running Locally

### Prerequisites
- Java 21
- Maven
- Docker

### Start Redis
```bash
docker run -d -p 6379:6379 --name redis-ratelimiter redis
```

### Run the app
```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`
Dashboard at `http://localhost:8080/dashboard.html`

### Run tests
```bash
mvn test
```

## Key Design Decisions

**Strategy Pattern** — both algorithms implement a common `RateLimitStrategy` interface. The algorithm is stored per client in the config, so it's swappable without touching the service or controller layer.

**Redis atomic operations** — Token Bucket uses `DECR` (atomic decrement) and Sliding Window uses sorted set operations (`ZREMRANGEBYSCORE`, `ZCARD`, `ZADD`). Both are atomic in Redis, which means concurrent requests are handled safely without explicit locking in Java.

**TTL-based cleanup** — Redis keys auto-expire after the window size. No background cleanup job needed. Inactive clients cost zero memory after their window expires.

**Distributed state** — because counters live in Redis and not in application memory, multiple instances of this service can run simultaneously and share the same rate limit state. A request to instance A and a request to instance B for the same client both count against the same Redis counter.

## Project Structure

```
src/main/java/com/ratelimiter/ratelimiter/
├── controller/
│   └── RateLimiterController.java
├── service/
│   └── RateLimiterService.java
├── strategy/
│   ├── RateLimitStrategy.java       (interface)
│   ├── TokenBucketStrategy.java
│   └── SlidingWindowStrategy.java
├── model/
│   ├── RateLimitConfig.java
│   └── RateLimitResponse.java
├── config/
│   └── RedisConfig.java
├── exception/
│   └── GlobalExceptionHandler.java
└── RateLimiterApplication.java

src/main/resources/
├── static/
│   └── dashboard.html
└── application.properties
```

## Algorithm Tradeoff Summary

| | Token Bucket | Sliding Window |
|---|---|---|
| Memory per client | O(1) | O(n) — grows with request count |
| Burst traffic | Allowed up to bucket size | Not allowed |
| Precision | Lower near window boundary | Higher — exact per-window count |
| Redis data structure | String (DECR) | Sorted Set (ZADD/ZCARD) |
| Best for | General APIs | Strict enforcement APIs |