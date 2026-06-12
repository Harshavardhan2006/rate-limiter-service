package com.ratelimiter.rate_limiter.strategy;

import com.ratelimiter.rate_limiter.model.RateLimitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("tokenBucket")
public class TokenBucketStrategy implements RateLimitStrategy {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String bucketKey(String clientId) { return "bucket:" + clientId; }
    private String refillKey(String clientId) { return "refill:" + clientId; }

    @Override
    public boolean isAllowed(String clientId, RateLimitConfig config) {
        long now = System.currentTimeMillis();
        long windowMs = config.getWindowSizeInSeconds() * 1000;

        String bKey = bucketKey(clientId);
        String rKey = refillKey(clientId);

        redisTemplate.opsForValue().setIfAbsent(bKey, config.getMaxRequests(), config.getWindowSizeInSeconds(), TimeUnit.SECONDS);
        redisTemplate.opsForValue().setIfAbsent(rKey, now, config.getWindowSizeInSeconds(), TimeUnit.SECONDS);

        Object lastRefillObj = redisTemplate.opsForValue().get(rKey);
        long lastRefill = lastRefillObj != null ? ((Number) lastRefillObj).longValue() : now;

        if (now - lastRefill >= windowMs) {
            redisTemplate.opsForValue().set(bKey, config.getMaxRequests(), config.getWindowSizeInSeconds(), TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(rKey, now, config.getWindowSizeInSeconds(), TimeUnit.SECONDS);
        }

        Object tokensObj = redisTemplate.opsForValue().get(bKey);
        int tokens = tokensObj != null ? ((Number) tokensObj).intValue() : 0;

        if (tokens > 0) {
            redisTemplate.opsForValue().decrement(bKey);
            return true;
        }
        return false;
    }

    @Override
    public void reset(String clientId) {
        redisTemplate.delete(bucketKey(clientId));
        redisTemplate.delete(refillKey(clientId));
    }
}