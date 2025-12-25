package com.statustracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Statuspage.io webhook payload structure
 * Handles both incident and component updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    
    // Event type: incident.created, incident.updated, component.updated, etc.
    private String type;
    
    // Incident payload (if type is incident-related)
    private Incident incident;
    
    // Component payload (if type is component-related)
    private Component component;
    
    // Meta information
    private Meta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Incident {
        private String id;
        private String name;
        private String status;
        private String impact;
        private String shortlink;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private ZonedDateTime resolvedAt;
        private ZonedDateTime monitoringAt;
        private List<Component> components;
        private List<IncidentUpdate> incidentUpdates;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Component {
        private String id;
        private String name;
        private String status;
        private String description;
        private String groupId;
        private Integer position;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncidentUpdate {
        private String id;
        private String status;
        private String body;
        private ZonedDateTime createdAt;
        private ZonedDateTime displayAt;
        private Boolean isTwitterUpdate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meta {
        private String timestampMs;
        private String pageId;
        private String providerId;
    }
    
    /**
     * Check if this is an incident webhook
     */
    public boolean isIncidentEvent() {
        return type != null && type.startsWith("incident.");
    }
    
    /**
     * Check if this is a component webhook
     */
    public boolean isComponentEvent() {
        return type != null && type.startsWith("component.");
    }
    
    /**
     * Get latest incident update message
     */
    public String getLatestUpdateMessage() {
        if (incident != null && incident.incidentUpdates != null && !incident.incidentUpdates.isEmpty()) {
            return incident.incidentUpdates.get(0).body;
        }
        return "No updates available";
    }
}
