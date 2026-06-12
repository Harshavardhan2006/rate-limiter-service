package com.ratelimiter.rate_limiter.model;

public class RateLimitResponse {
    private boolean allowed;
    private String clientId;
    private String message;
    private long retryAfterSeconds;

    public RateLimitResponse(boolean allowed, String clientId, String message, long retryAfterSeconds) {
        this.allowed = allowed;
        this.clientId = clientId;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public void setRetryAfterSeconds(long retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
}