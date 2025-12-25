package com.statustracker.service;

import com.statustracker.entity.IncidentLog;
import com.statustracker.model.StatusUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Change Detection Service
 * ✅ FIXED: No dependencies on other services (single responsibility)
 */
@Slf4j
@Service
public class ChangeDetectionService {

    /**
     * Intelligent change detection:
     * - Only notify if status actually changed
     * - Ignore duplicate messages
     * - Track state transitions
     */
    public boolean hasStatusChanged(IncidentLog existingIncident, StatusUpdate update) {
        // Status change detected
        boolean statusChanged = !existingIncident.getStatus().equalsIgnoreCase(update.getStatus());

        // Message change detected
        boolean messageChanged = !existingIncident.getStatusMessage()
                .equalsIgnoreCase(update.getStatusMessage());

        // Only consider it changed if actual status changed or message significantly changed
        if (statusChanged) {
            log.info("Status transition: {} -> {}", existingIncident.getStatus(), update.getStatus());
            return true;
        }

        // Allow message updates but only if it's been more than 1 minute
        if (messageChanged) {
            LocalDateTime lastUpdate = existingIncident.getUpdatedAt();
            if (lastUpdate.plusMinutes(1).isBefore(LocalDateTime.now())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get severity level
     */
    public String mapImpactToSeverity(String impact) {
        return switch (impact.toLowerCase()) {
            case "critical" -> "critical";
            case "major" -> "major";
            case "minor" -> "minor";
            case "maintenance" -> "maintenance";
            default -> "unknown";
        };
    }
}
