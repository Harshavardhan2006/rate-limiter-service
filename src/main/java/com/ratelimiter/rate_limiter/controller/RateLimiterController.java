package com.ratelimiter.rate_limiter.controller;

import com.ratelimiter.rate_limiter.model.RateLimitConfig;
import com.ratelimiter.rate_limiter.model.RateLimitResponse;
import com.ratelimiter.rate_limiter.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;

import java.util.Map;

@RestController
@RequestMapping("/api/ratelimiter")
public class RateLimiterController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping("/register")
    public ResponseEntity<String> registerClient(@RequestBody RateLimitConfig config) {
        rateLimiterService.registerClient(config);
        return ResponseEntity.ok("Client " + config.getClientId() + " registered successfully");
    }

    @GetMapping("/check/{clientId}")
    public ResponseEntity<RateLimitResponse> checkLimit(@PathVariable String clientId) {
        RateLimitResponse response = rateLimiterService.checkLimit(clientId);
        if (!response.isAllowed()) {
            return ResponseEntity.status(429).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{clientId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String clientId) {
        return ResponseEntity.ok(rateLimiterService.getStats(clientId));
    }

    @GetMapping("/admin/rules")
    public ResponseEntity<Map<String, Object>> getAllRules() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin endpoint - all registered clients");
        response.put("totalClients", rateLimiterService.getAllConfigs().size());
        response.put("clients", rateLimiterService.getAllConfigs());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/rules/{clientId}")
    public ResponseEntity<String> updateRule(@PathVariable String clientId, @RequestBody RateLimitConfig config) {
        config.setClientId(clientId);
        rateLimiterService.registerClient(config);
        return ResponseEntity.ok("Rules updated for client: " + clientId);
    }

    @DeleteMapping("/admin/rules/{clientId}")
    public ResponseEntity<String> deleteRule(@PathVariable String clientId) {
        rateLimiterService.removeClient(clientId);
        return ResponseEntity.ok("Client " + clientId + " removed successfully");
    }

}