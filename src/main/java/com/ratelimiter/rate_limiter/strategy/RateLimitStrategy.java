package com.ratelimiter.rate_limiter.strategy;

import com.ratelimiter.rate_limiter.model.RateLimitConfig;

public interface RateLimitStrategy {
    boolean isAllowed(String clientId, RateLimitConfig config);
    void reset(String clientId);
}