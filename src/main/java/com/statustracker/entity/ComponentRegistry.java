package com.statustracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persistent storage for component registry
 * Maintains a registry of all components from all providers
 * Allows tracking component status changes over time
 */
@Entity
@Table(name = "component_registry", indexes = {
    @Index(name = "idx_component_id", columnList = "component_id, provider"),
    @Index(name = "idx_provider_status", columnList = "provider, current_status"),
    @Index(name = "idx_last_checked", columnList = "last_checked")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComponentRegistry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String componentId;
    
    @Column(nullable = false)
    private String provider;
    
    @Column(nullable = false)
    private String componentName;
    
    private String currentStatus; // operational, degraded_performance, partial_outage, major_outage
    
    @Lob
    private String description;
    
    private String groupId;
    private String groupName;
    private Integer position;
    
    // Performance metrics
    private String uptime; // e.g., "99.99%"
    private Integer estimatedAffectedUsers;
    private String impactLevel; // critical, major, minor
    
    // Tracking
    private LocalDateTime lastCheckedAt;
    private LocalDateTime lastStatusChangeAt;
    private LocalDateTime lastIncidentAt;
    
    // Related incident
    private String relatedIncidentId;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // For optimistic locking
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if component is degraded
     */
    public boolean isDegraded() {
        return currentStatus != null && 
               !currentStatus.equalsIgnoreCase("operational") &&
               !currentStatus.equalsIgnoreCase("under_maintenance");
    }
    
    /**
     * Get severity level
     */
    public String getSeverity() {
        return switch (currentStatus != null ? currentStatus.toLowerCase() : "unknown") {
            case "major_outage" -> "critical";
            case "partial_outage" -> "major";
            case "degraded_performance" -> "minor";
            case "under_maintenance" -> "maintenance";
            case "operational" -> "healthy";
            default -> "unknown";
        };
    }
    
    /**
     * Get human-readable status
     */
    public String getHumanReadableStatus() {
        return switch (currentStatus != null ? currentStatus.toLowerCase() : "unknown") {
            case "major_outage" -> "🔴 Major Outage";
            case "partial_outage" -> "🟠 Partial Outage";
            case "degraded_performance" -> "🟡 Degraded Performance";
            case "under_maintenance" -> "🔵 Under Maintenance";
            case "operational" -> "🟢 Operational";
            default -> "❓ Unknown";
        };
    }
}
