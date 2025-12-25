package com.statustracker.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.statustracker.model.ComponentStatus;
import com.statustracker.model.StatusUpdate;
import com.statustracker.service.StatusPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Statuspage.io Provider
 * Can work with any Statuspage.io based status page
 * Provides template for adding new providers quickly
 */
@Slf4j
@Service
public class GenericStatuspageProvider implements StatusPageProvider {
    
    private String providerName;
    private String baseUrl;
    private String pageId;
    private String webhookPath;
    private long lastSyncTime = 0;
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StatusPageService statusPageService;

    public GenericStatuspageProvider() {

    }

    /**
     * Constructor for generic provider
     */


    public GenericStatuspageProvider(String providerName, String baseUrl, String pageId, String webhookPath) {
        this.providerName = providerName;
        this.baseUrl = baseUrl;
        this.pageId = pageId;
        this.webhookPath = webhookPath;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getPageId() {
        return pageId;
    }

    @Override
    public String getWebhookUrl() {
        return webhookPath;
    }

    @Override
    public List<StatusUpdate> getIncidents() {
        List<StatusUpdate> incidents = new ArrayList<>();
        
        try {
            String url = baseUrl + "/pages/" + pageId + "/incidents.json";
            
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode incidentsNode = root.get("incidents");
                
                if (incidentsNode.isArray()) {
                    incidentsNode.forEach(incident -> {
                        StatusUpdate update = parseIncident(incident);
                        incidents.add(update);
                    });
                }
            }
            
            log.debug("Retrieved {} incidents from {}", incidents.size(), providerName);
        } catch (Exception e) {
            log.error("Error fetching {} incidents: {}", providerName, e.getMessage(), e);
        }
        
        return incidents;
    }

    @Override
    public List<ComponentStatus> getComponents() {
        List<ComponentStatus> components = new ArrayList<>();
        
        try {
            String url = baseUrl + "/pages/" + pageId + "/components.json";
            
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode componentsNode = root.get("components");
                
                if (componentsNode.isArray()) {
                    componentsNode.forEach(component -> {
                        ComponentStatus status = parseComponent(component);
                        components.add(status);
                    });
                }
            }
            
            log.debug("Retrieved {} components from {}", components.size(), providerName);
        } catch (Exception e) {
            log.error("Error fetching {} components: {}", providerName, e.getMessage(), e);
        }
        
        return components;
    }

    @Override
    public void syncStatus() {
        try {
            log.info("Syncing {} status...", providerName);
            
            List<StatusUpdate> incidents = getIncidents();
            for (StatusUpdate incident : incidents) {
                statusPageService.processStatusUpdate(incident);
            }
            
            lastSyncTime = System.currentTimeMillis();
            log.info("{} status sync completed", providerName);
        } catch (Exception e) {
            log.error("Error syncing {} status: {}", providerName, e.getMessage(), e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            String url = baseUrl + "/pages/" + pageId + "/status.json";
            
            String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.warn("{} provider health check failed: {}", providerName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            return signature != null && !signature.isEmpty();
        } catch (Exception e) {
            log.error("Webhook signature validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getLastSyncTime() {
        return lastSyncTime;
    }

    /**
     * Parse incident from JSON node
     */
    private StatusUpdate parseIncident(JsonNode node) {
        StatusUpdate update = new StatusUpdate();
        
        update.setServiceId(node.get("id").asText());
        update.setProductName(node.get("name").asText());
        update.setStatus(node.get("status").asText());
        update.setIncidentUrl(node.get("shortlink").asText());
        update.setProvider(providerName);
        update.setSeverity(node.get("impact").asText());
        update.setTimestamp(LocalDateTime.now());
        
        JsonNode updates = node.get("incident_updates");
        if (updates.isArray() && updates.size() > 0) {
            update.setStatusMessage(updates.get(0).get("body").asText());
        } else {
            update.setStatusMessage("No updates");
        }
        
        return update;
    }

    /**
     * Parse component from JSON node
     */
    private ComponentStatus parseComponent(JsonNode node) {
        ComponentStatus status = new ComponentStatus();
        
        status.setComponentId(node.get("id").asText());
        status.setComponentName(node.get("name").asText());
        status.setProvider(providerName);
        status.setStatus(node.get("status").asText());
        status.setDescription(node.get("description").asText());
        status.setLastCheckedAt(LocalDateTime.now());
        
        return status;
    }
}
