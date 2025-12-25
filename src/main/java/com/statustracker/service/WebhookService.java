package com.statustracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.statustracker.model.StatusUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WebhookService {
    
    @Autowired
    private StatusPageService statusPageService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Handle incoming webhook payloads from Statuspage.io
     * Statuspage sends incident updates and component updates
     */
    public void handleStatusPageWebhook(String payload, String provider) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            
            // Check if it's an incident webhook
            if (root.has("incident")) {
                processIncidentWebhook(root, provider);
            }
            // Check if it's a component webhook
            else if (root.has("component")) {
                processComponentWebhook(root, provider);
            }
            else {
                log.warn("Unknown webhook payload structure from {}: {}", provider, payload);
            }
        } catch (Exception e) {
            log.error("Error processing webhook from {}: {}", provider, e.getMessage(), e);
        }
    }

    private void processIncidentWebhook(JsonNode root, String provider) {
        JsonNode incidentNode = root.get("incident");
        
        String incidentId = incidentNode.get("id").asText();
        String incidentName = incidentNode.get("name").asText();
        String incidentStatus = incidentNode.get("status").asText();
        String incidentImpact = incidentNode.get("impact").asText();
        String incidentUrl = incidentNode.get("shortlink").asText();
        
        // Get latest update
        JsonNode updates = incidentNode.get("incident_updates");
        String latestMessage = "";
        if (updates.size() > 0) {
            latestMessage = updates.get(0).get("body").asText();
        }
        
        // Get affected components
        JsonNode components = incidentNode.get("components");
        List<String> affectedComponents = new ArrayList<>();
        components.forEach(comp -> {
            affectedComponents.add(comp.get("name").asText());
        });
        
        // Determine severity based on impact
        String severity = mapImpactToSeverity(incidentImpact);
        
        // Create status update
        StatusUpdate update = new StatusUpdate();
        update.setServiceId(incidentId);
        update.setProductName(incidentName);
        update.setStatus(incidentStatus);
        update.setStatusMessage(latestMessage);
        update.setIncidentUrl(incidentUrl);
        update.setProvider(provider);
        update.setSeverity(severity);
        update.setTimestamp(LocalDateTime.now());
        
        statusPageService.processStatusUpdate(update);
    }

    private void processComponentWebhook(JsonNode root, String provider) {
        JsonNode componentNode = root.get("component");
        
        String componentId = componentNode.get("id").asText();
        String componentName = componentNode.get("name").asText();
        String componentStatus = componentNode.get("status").asText();
        
        // Map component status to incident-like status
        String status = mapComponentStatusToIncidentStatus(componentStatus);
        
        StatusUpdate update = new StatusUpdate();
        update.setServiceId(componentId);
        update.setProductName(componentName);
        update.setStatus(status);
        update.setStatusMessage("Component status changed to: " + componentStatus);
        update.setProvider(provider);
        update.setSeverity(mapComponentStatusToSeverity(componentStatus));
        update.setTimestamp(LocalDateTime.now());
        
        statusPageService.processStatusUpdate(update);
    }

    private String mapImpactToSeverity(String impact) {
        return switch (impact.toLowerCase()) {
            case "critical" -> "critical";
            case "major" -> "major";
            case "minor" -> "minor";
            case "maintenance" -> "maintenance";
            default -> "unknown";
        };
    }

    private String mapComponentStatusToSeverity(String status) {
        return switch (status.toLowerCase()) {
            case "major_outage" -> "critical";
            case "partial_outage" -> "major";
            case "degraded_performance" -> "minor";
            case "operational" -> "resolved";
            default -> "unknown";
        };
    }

    private String mapComponentStatusToIncidentStatus(String componentStatus) {
        return switch (componentStatus.toLowerCase()) {
            case "major_outage", "partial_outage" -> "investigating";
            case "degraded_performance" -> "degraded";
            case "operational" -> "resolved";
            default -> "monitoring";
        };
    }
}
