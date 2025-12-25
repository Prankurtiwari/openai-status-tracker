package com.statustracker.listener;

import com.statustracker.model.StatusUpdate;
import com.statustracker.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for status update events
 * Listens to status changes and triggers appropriate actions
 */
@Slf4j
@Component
public class StatusUpdateListener {
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Listen for new status update events
     * This allows loose coupling between services
     */
    @EventListener
    @Async
    public void onStatusUpdate(StatusUpdateEvent event) {
        try {
            StatusUpdate update = event.getStatusUpdate();
            log.info("Processing status update event: {}", update.getProductName());
            
            if (event.isNewIncident()) {
                notificationService.notifyNewIncident(update);
            } else if (event.isResolved()) {
                notificationService.notifyIncidentResolved(update);
            } else {
                notificationService.notifyStatusChange(update);
            }
            
        } catch (Exception e) {
            log.error("Error processing status update event: {}", e.getMessage(), e);
        }
    }

    /**
     * Listen for critical incidents
     * Trigger extra alerting for critical severity
     */
    @EventListener
    @Async
    public void onCriticalIncident(StatusUpdateEvent event) {
        if ("critical".equalsIgnoreCase(event.getStatusUpdate().getSeverity())) {
            try {
                StatusUpdate update = event.getStatusUpdate();
                log.warn("CRITICAL INCIDENT DETECTED: {}", update.getProductName());
                
                // Trigger escalation logic here
                // Could send alerts to on-call engineers, etc.
                
            } catch (Exception e) {
                log.error("Error handling critical incident: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Listen for incident resolution
     * Log resolution metrics
     */
    @EventListener
    @Async
    public void onIncidentResolution(StatusUpdateEvent event) {
        if (event.isResolved()) {
            try {
                StatusUpdate update = event.getStatusUpdate();
                log.info("Incident resolved: {} (Duration: {}ms)", 
                    update.getProductName(), 
                    event.getResolutionTimeMs());
                
            } catch (Exception e) {
                log.error("Error processing incident resolution: {}", e.getMessage(), e);
            }
        }
    }
}

/**
 * Event class for status updates
 * Published when status changes occur
 */
@Slf4j
class StatusUpdateEvent {
    private final StatusUpdate statusUpdate;
    private final boolean isNewIncident;
    private final boolean isResolved;
    private final long resolutionTimeMs;
    private final long timestamp;

    public StatusUpdateEvent(StatusUpdate statusUpdate, boolean isNewIncident, boolean isResolved) {
        this.statusUpdate = statusUpdate;
        this.isNewIncident = isNewIncident;
        this.isResolved = isResolved;
        this.resolutionTimeMs = 0;
        this.timestamp = System.currentTimeMillis();
    }

    public StatusUpdateEvent(StatusUpdate statusUpdate, boolean isNewIncident, boolean isResolved, long resolutionTimeMs) {
        this.statusUpdate = statusUpdate;
        this.isNewIncident = isNewIncident;
        this.isResolved = isResolved;
        this.resolutionTimeMs = resolutionTimeMs;
        this.timestamp = System.currentTimeMillis();
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }

    public boolean isNewIncident() {
        return isNewIncident;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public long getResolutionTimeMs() {
        return resolutionTimeMs;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
