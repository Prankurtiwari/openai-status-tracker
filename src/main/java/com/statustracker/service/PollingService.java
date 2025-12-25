package com.statustracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.statustracker.model.StatusUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Polling Service - fetches status updates
 * Does NOT depend on StatusPageService (removed circular dependency)
 * Returns data for caller to process
 */
@Slf4j
@Service
public class PollingService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.providers.openai.baseUrl}")
    private String baseUrl;

    @Value("${app.providers.openai.pageId}")
    private String pageId;

    /**
     * Poll OpenAI status and return incidents
     * ✅ FIXED: Now returns data instead of calling StatusPageService
     */
    public List<StatusUpdate> pollOpenAiStatus() {
        log.debug("Polling OpenAI status page...");

        List<StatusUpdate> incidents = new ArrayList<>();

        try {
            String incidentsUrl = baseUrl + "/pages/" + pageId + "/incidents.json";

            String response = webClient.get()
                    .uri(incidentsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.startsWith("<!DOCTYPE html>")) {
                incidents = parsePolledIncidents(response);
            }

        } catch (Exception e) {
            log.error("Polling error: {}", e.getMessage(), e);
        }

        return incidents;
    }

    /**
     * Parse polled incidents from API response
     * ✅ Returns data instead of processing directly
     */
    private List<StatusUpdate> parsePolledIncidents(String response) {
        List<StatusUpdate> incidents = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode incidentsNode = root.get("incidents");

            if (incidentsNode != null && incidentsNode.isArray()) {
                incidentsNode.forEach(incident -> {
                    String incidentId = incident.get("id").asText();
                    String name = incident.get("name").asText();
                    String status = incident.get("status").asText();
                    String impact = incident.get("impact").asText();
                    String shortlink = incident.get("shortlink").asText();

                    JsonNode updates = incident.get("incident_updates");
                    String message = "";
                    if (updates.isArray() && updates.size() > 0) {
                        message = updates.get(0).get("body").asText();
                    }

                    StatusUpdate update = new StatusUpdate();
                    update.setServiceId(incidentId);
                    update.setProductName(name);
                    update.setStatus(status);
                    update.setStatusMessage(message);
                    update.setProvider("openai");
                    update.setSeverity(impact);
                    update.setTimestamp(LocalDateTime.now());
                    update.setIncidentUrl(shortlink);

                    incidents.add(update);
                });
            }
        } catch (Exception e) {
            log.error("Error parsing polled incidents: {}", e.getMessage(), e);
        }

        return incidents;
    }
}
