package com.ratelimiter.rate_limiter.strategy;

import com.ratelimiter.rate_limiter.model.RateLimitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("slidingWindow")
public class SlidingWindowStrategy implements RateLimitStrategy {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String windowKey(String clientId) { return "window:" + clientId; }

    @Override
    public boolean isAllowed(String clientId, RateLimitConfig config) {
        long now = System.currentTimeMillis();
        long windowMs = config.getWindowSizeInSeconds() * 1000;
        long windowStart = now - windowMs;
        String key = windowKey(clientId);

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        Long requestCount = redisTemplate.opsForZSet().zCard(key);
        long count = requestCount != null ? requestCount : 0;

        if (count < config.getMaxRequests()) {
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            redisTemplate.expire(key, config.getWindowSizeInSeconds(), TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    @Override
    public void reset(String clientId) {
        redisTemplate.delete(windowKey(clientId));
    }
}