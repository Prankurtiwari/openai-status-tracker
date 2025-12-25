package com.statustracker.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.statustracker.model.ComponentStatus;
import com.statustracker.model.StatusUpdate;
import com.statustracker.service.StatusPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI Status Page Provider Implementation
 * Handles OpenAI's Statuspage.io integration
 */
@Slf4j
@Service
public class OpenAiStatusProvider implements StatusPageProvider {
    
    private static final String PROVIDER_NAME = "openai";
    private long lastSyncTime = 0;
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StatusPageService statusPageService;
    
    @Value("${app.providers.openai.baseUrl}")
    private String baseUrl;
    
    @Value("${app.providers.openai.pageId}")
    private String pageId;
    
    @Value("${app.providers.openai.webhookPath}")
    private String webhookPath;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
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
            
            log.debug("Retrieved {} incidents from OpenAI", incidents.size());
        } catch (Exception e) {
            log.error("Error fetching OpenAI incidents: {}", e.getMessage(), e);
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
            
            log.debug("Retrieved {} components from OpenAI", components.size());
        } catch (Exception e) {
            log.error("Error fetching OpenAI components: {}", e.getMessage(), e);
        }
        
        return components;
    }

    @Override
    public void syncStatus() {
        try {
            log.info("Syncing OpenAI status...");
            
            List<StatusUpdate> incidents = getIncidents();
            for (StatusUpdate incident : incidents) {
                statusPageService.processStatusUpdate(incident);
            }
            
            lastSyncTime = System.currentTimeMillis();
            log.info("OpenAI status sync completed");
        } catch (Exception e) {
            log.error("Error syncing OpenAI status: {}", e.getMessage(), e);
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
        } catch (WebClientException e) {
            log.warn("OpenAI provider health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        // Implement OpenAI's webhook signature validation
        // For now, basic validation
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
        update.setProvider(PROVIDER_NAME);
        update.setSeverity(node.get("impact").asText());
        update.setTimestamp(LocalDateTime.now());
        
        // Get latest update message
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
        status.setProvider(PROVIDER_NAME);
        status.setStatus(node.get("status").asText());
        status.setDescription(node.get("description").asText());
        status.setPosition(node.get("position").asInt());
        status.setLastCheckedAt(LocalDateTime.now());
        
        return status;
    }
}
