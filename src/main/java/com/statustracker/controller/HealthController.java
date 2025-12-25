package com.statustracker.controller;

import com.statustracker.entity.IncidentLog;
import com.statustracker.service.ProviderRegistry;
import com.statustracker.service.StatusPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Health check and monitoring endpoints
 * Provides comprehensive system health status
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @Autowired
    private ProviderRegistry providerRegistry;
    
    @Autowired
    private StatusPageService statusPageService;
    
    @Autowired
    private HealthEndpoint healthEndpoint;

    /**
     * Simple health check endpoint
     * Returns 200 if application is up
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "OpenAI Status Tracker");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check with component status
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        
        HealthComponent health = healthEndpoint.health();
        response.put("status", health.getStatus().toString());
        response.put("components", health);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Provider health status
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> providersHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalProviders", providerRegistry.getProviderCount());
        response.put("providers", providerRegistry.getAllProvidersHealth());
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * System status including active incidents
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> systemStatus() {
        Map<String, Object> response = new HashMap<>();
        
        List<IncidentLog> activeIncidents = statusPageService.getActiveIncidents();
        
        response.put("status", activeIncidents.isEmpty() ? "HEALTHY" : "DEGRADED");
        response.put("activeIncidents", activeIncidents.size());
        response.put("incidents", activeIncidents);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get incident summary by provider
     */
    @GetMapping("/incidents-by-provider")
    public ResponseEntity<Map<String, Object>> incidentsByProvider() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> incidentCounts = new HashMap<>();
        
        for (String provider : providerRegistry.getProviderNames()) {
            List<IncidentLog> incidents = statusPageService.getRecentIncidents(provider, 24);
            incidentCounts.put(provider, incidents.size());
        }
        
        response.put("incidentCounts", incidentCounts);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe for Kubernetes
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        return ResponseEntity.ok(
            Collections.singletonMap("status", "LIVE")
        );
    }

    /**
     * Readiness probe for Kubernetes
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> readiness() {
        // Check if critical services are ready
        if (providerRegistry.getProviderCount() == 0) {
            return ResponseEntity.status(503).body(
                Collections.singletonMap("status", "NOT_READY")
            );
        }
        
        return ResponseEntity.ok(
            Collections.singletonMap("status", "READY")
        );
    }

    /**
     * Startup probe for Kubernetes
     */
    @GetMapping("/startup")
    public ResponseEntity<Map<String, String>> startup() {
        return ResponseEntity.ok(
            Collections.singletonMap("status", "STARTED")
        );
    }
}
