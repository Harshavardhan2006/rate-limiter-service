package com.ratelimiter.rate_limiter.model;

public class RateLimitConfig {
    private String clientId;
    private int maxRequests;
    private long windowSizeInSeconds;
    private String strategy = "tokenBucket";

    public RateLimitConfig() {}

    public RateLimitConfig(String clientId, int maxRequests, long windowSizeInSeconds, String strategy) {
        this.clientId = clientId;
        this.maxRequests = maxRequests;
        this.windowSizeInSeconds = windowSizeInSeconds;
        this.strategy = strategy;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public int getMaxRequests() { return maxRequests; }
    public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }
    public long getWindowSizeInSeconds() { return windowSizeInSeconds; }
    public void setWindowSizeInSeconds(long windowSizeInSeconds) { this.windowSizeInSeconds = windowSizeInSeconds; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
}