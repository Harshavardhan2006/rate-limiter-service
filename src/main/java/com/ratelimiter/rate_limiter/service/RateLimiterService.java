package com.ratelimiter.rate_limiter.service;

import com.ratelimiter.rate_limiter.model.RateLimitConfig;
import com.ratelimiter.rate_limiter.model.RateLimitResponse;
import com.ratelimiter.rate_limiter.strategy.RateLimitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RateLimiterService {

    private final Map<String, RateLimitConfig> clientConfigs = new HashMap<>();

    @Autowired
    private Map<String, RateLimitStrategy> strategies;

    public void registerClient(RateLimitConfig config) {
        if (config.getClientId() == null || config.getClientId().isBlank()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (config.getMaxRequests() <= 0) {
            throw new IllegalArgumentException("maxRequests must be greater than 0");
        }
        if (config.getWindowSizeInSeconds() <= 0) {
            throw new IllegalArgumentException("windowSizeInSeconds must be greater than 0");
        }
        if (!strategies.containsKey(config.getStrategy())) {
            throw new IllegalArgumentException("Unknown strategy: " + config.getStrategy() + ". Use tokenBucket or slidingWindow");
        }
        clientConfigs.put(config.getClientId(), config);
    }

    public RateLimitResponse checkLimit(String clientId) {
        if (!clientConfigs.containsKey(clientId)) {
            return new RateLimitResponse(false, clientId, "Client not registered", 0);
        }

        RateLimitConfig config = clientConfigs.get(clientId);
        RateLimitStrategy strategy = strategies.get(config.getStrategy());

        if (strategy == null) {
            return new RateLimitResponse(false, clientId, "Unknown strategy: " + config.getStrategy(), 0);
        }

        boolean allowed = strategy.isAllowed(clientId, config);

        if (!allowed) {
            return new RateLimitResponse(false, clientId, "Rate limit exceeded", config.getWindowSizeInSeconds());
        }
        return new RateLimitResponse(true, clientId, "Request allowed", 0);
    }

    public Map<String, Object> getStats(String clientId) {
        Map<String, Object> stats = new HashMap<>();
        if (!clientConfigs.containsKey(clientId)) {
            stats.put("error", "Client not registered");
            return stats;
        }
        RateLimitConfig config = clientConfigs.get(clientId);
        stats.put("clientId", clientId);
        stats.put("maxRequests", config.getMaxRequests());
        stats.put("windowSizeInSeconds", config.getWindowSizeInSeconds());
        stats.put("strategy", config.getStrategy());
        return stats;
    }

    public Map<String, RateLimitConfig> getAllConfigs() {
        return clientConfigs;
    }

    public void removeClient(String clientId) {
        if (!clientConfigs.containsKey(clientId)) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }
        clientConfigs.remove(clientId);
        RateLimitStrategy strategy = strategies.get(clientConfigs.getOrDefault(clientId, new RateLimitConfig()).getStrategy());
        if (strategy != null) {
            strategy.reset(clientId);
        }
    }

}