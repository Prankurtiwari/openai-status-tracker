package com.statustracker.service;

import com.statustracker.entity.IncidentLog;
import com.statustracker.model.StatusUpdate;
import com.statustracker.repository.IncidentLogRepository;
import com.statustracker.repository.StatusChangeLogRepository;
import com.statustracker.util.DateFormatter;
import com.statustracker.util.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Status Page Service - Main service for processing status updates
 * ✅ FIXED: Direct dependency on PollingService only (no circular reference)
 */
@Slf4j
@Service
@Transactional
public class StatusPageService {

    @Autowired
    private IncidentLogRepository incidentLogRepository;

    @Autowired
    private StatusChangeLogRepository statusChangeLogRepository;

    @Autowired
    private ChangeDetectionService changeDetectionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PollingService pollingService;

    /**
     * Main entry point for processing status updates
     */
    public void processStatusUpdate(StatusUpdate update) {
        log.info("Processing status update for {} - {}", update.getProductName(), update.getStatus());

        // Check if this is a duplicate using hash
        String hashCode = HashUtil.generateHash(update);

        Optional<IncidentLog> existingIncident = incidentLogRepository
                .findByIncidentIdAndProvider(update.getServiceId(), update.getProvider());

        if (existingIncident.isPresent()) {
            IncidentLog incident = existingIncident.get();

            // Check if status actually changed
            if (changeDetectionService.hasStatusChanged(incident, update)) {
                updateIncident(incident, update, hashCode);
                logConsoleOutput(update, true);
                notificationService.notifyStatusChange(update);
            } else {
                log.debug("Duplicate status update ignored: {}", hashCode);
            }
        } else {
            // New incident
            createNewIncident(update, hashCode);
            logConsoleOutput(update, true);
            notificationService.notifyNewIncident(update);
        }
    }

    /**
     * ✅ NEW METHOD: Fetch and process incidents from polling
     * Caller can now invoke polling explicitly
     */
    public void syncIncidentsFromPolling(String provider) {
        try {
            log.info("Syncing incidents from {} via polling...", provider);

            // Get incidents from polling service
            List<StatusUpdate> incidents = pollingService.pollOpenAiStatus();

            // Process each incident
            for (StatusUpdate incident : incidents) {
                processStatusUpdate(incident);
            }

            log.info("Polling sync completed for {}: {} incidents", provider, incidents.size());
        } catch (Exception e) {
            log.error("Error syncing from polling: {}", e.getMessage(), e);
        }
    }

    private void createNewIncident(StatusUpdate update, String hashCode) {
        IncidentLog incident = new IncidentLog();
        incident.setIncidentId(update.getServiceId());
        incident.setProvider(update.getProvider());
        incident.setServiceName(update.getProductName());
        incident.setStatus(update.getStatus());
        incident.setStatusMessage(update.getStatusMessage());
        incident.setSeverity(update.getSeverity());
        incident.setIncidentUrl(update.getIncidentUrl());
        incident.setHashCode(hashCode);
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        incidentLogRepository.save(incident);
        log.info("New incident created: {}", update.getProductName());
    }

    private void updateIncident(IncidentLog incident, StatusUpdate update, String hashCode) {
        incident.setStatus(update.getStatus());
        incident.setStatusMessage(update.getStatusMessage());
        incident.setHashCode(hashCode);
        incident.setUpdatedAt(LocalDateTime.now());

        if ("resolved".equalsIgnoreCase(update.getStatus())) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        incidentLogRepository.save(incident);
        log.info("Incident updated: {}", update.getProductName());
    }

    private void logConsoleOutput(StatusUpdate update, boolean isNewOrChanged) {
        String timestamp = DateFormatter.formatTimestamp(LocalDateTime.now());
        String output = String.format("[%s] Product: %s - %s",
                timestamp,
                update.getProductName(),
                update.getStatusMessage());

        System.out.println(output);
        log.info(output);
    }

    public List<IncidentLog> getActiveIncidents() {
        return incidentLogRepository.findActiveIncidents();
    }

    public List<IncidentLog> getRecentIncidents(String provider, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return incidentLogRepository.findByProviderAndCreatedAtAfter(provider, since);
    }
}
