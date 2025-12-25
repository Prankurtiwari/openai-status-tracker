package com.statustracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the status of a single component/service
 * Used for tracking individual component health
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentStatus {
    
    private String componentId;
    private String componentName;
    private String provider;
    
    // Status levels: operational, degraded_performance, partial_outage, major_outage, under_maintenance
    private String status;
    
    // Human-readable description
    private String description;
    
    // Component group (API, Infrastructure, etc.)
    private String groupId;
    private String groupName;
    
    // Ordering for UI
    private int position;
    
    // Detailed metrics
    private String uptime; // e.g., "99.99%"
    private LocalDateTime lastCheckedAt;
    private LocalDateTime lastIncidentAt;
    
    // Impact assessment
    private int affectedUsers; // estimated
    private String impactLevel; // critical, major, minor
    
    // Related incident
    private String relatedIncidentId;
    private String incidentStatus; // investigating, identified, monitoring, resolved
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Get severity based on status
     */
    public String getSeverity() {
        return switch (this.status.toLowerCase()) {
            case "major_outage" -> "critical";
            case "partial_outage" -> "major";
            case "degraded_performance" -> "minor";
            case "under_maintenance" -> "maintenance";
            case "operational" -> "healthy";
            default -> "unknown";
        };
    }
    
    /**
     * Check if component is degraded
     */
    public boolean isDegraded() {
        return !this.status.equalsIgnoreCase("operational") && 
               !this.status.equalsIgnoreCase("under_maintenance");
    }
    
    /**
     * Check if component is critical
     */
    public boolean isCritical() {
        return this.status.equalsIgnoreCase("major_outage") && 
               this.impactLevel != null && this.impactLevel.equalsIgnoreCase("critical");
    }
}
