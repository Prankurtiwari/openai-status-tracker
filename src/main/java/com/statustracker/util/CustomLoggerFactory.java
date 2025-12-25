package com.statustracker.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logger factory for structured logging
 * Provides consistent logging format across the application
 */
@Slf4j
public class CustomLoggerFactory {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Get logger for a class
     */
    public static Logger getLogger(Class<?> clazz) {
        return CustomLoggerFactory.getLogger(clazz);
    }

    /**
     * Log info with structured format
     */
    public static void logInfo(String message, Object... args) {
        log.info(formatMessage(message, args));
    }

    /**
     * Log warning with structured format
     */
    public static void logWarning(String message, Object... args) {
        log.warn(formatMessage(message, args));
    }

    /**
     * Log error with structured format
     */
    public static void logError(String message, Throwable throwable, Object... args) {
        log.error(formatMessage(message, args), throwable);
    }

    /**
     * Log debug with structured format
     */
    public static void logDebug(String message, Object... args) {
        log.debug(formatMessage(message, args));
    }

    /**
     * Log incident detection
     */
    public static void logIncident(String provider, String service, String status, String message) {
        String formattedLog = String.format(
            "[%s] [%s] Product: %s | Status: %s | Message: %s",
            getTimestamp(),
            provider.toUpperCase(),
            service,
            status.toUpperCase(),
            message
        );
        
        log.info(formattedLog);
        System.out.println(formattedLog); // Also print to console
    }

    /**
     * Log status change
     */
    public static void logStatusChange(String provider, String service, String oldStatus, String newStatus) {
        String message = String.format(
            "[%s] [%s] %s: %s → %s",
            getTimestamp(),
            provider.toUpperCase(),
            service,
            oldStatus.toUpperCase(),
            newStatus.toUpperCase()
        );
        
        log.info(message);
        System.out.println(message);
    }

    /**
     * Log webhook received
     */
    public static void logWebhookReceived(String provider, String type, String details) {
        String message = String.format(
            "[%s] Webhook from %s [%s]: %s",
            getTimestamp(),
            provider,
            type,
            details
        );
        
        log.debug(message);
    }

    /**
     * Log polling activity
     */
    public static void logPolling(String provider, int incidentCount) {
        String message = String.format(
            "[%s] Polling %s: %d incidents found",
            getTimestamp(),
            provider,
            incidentCount
        );
        
        log.debug(message);
    }

    /**
     * Log health check
     */
    public static void logHealthCheck(String provider, boolean isHealthy) {
        String status = isHealthy ? "✓ UP" : "✗ DOWN";
        String message = String.format(
            "[%s] Health Check %s: %s",
            getTimestamp(),
            provider,
            status
        );
        
        log.info(message);
    }

    /**
     * Log performance metrics
     */
    public static void logMetrics(String action, long durationMs) {
        String message = String.format(
            "[%s] %s completed in %dms",
            getTimestamp(),
            action,
            durationMs
        );
        
        log.debug(message);
    }

    /**
     * Format log message with timestamp
     */
    private static String formatMessage(String message, Object... args) {
        return String.format("[%s] %s", getTimestamp(), 
            args.length > 0 ? String.format(message, args) : message);
    }

    /**
     * Get formatted timestamp
     */
    private static String getTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    /**
     * Create performance timer for measuring operation duration
     */
    public static PerformanceTimer startTimer(String operationName) {
        return new PerformanceTimer(operationName);
    }

    /**
     * Performance timer utility class
     */
    public static class PerformanceTimer {
        private final String operationName;
        private final long startTime;

        public PerformanceTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
        }

        public void stop() {
            long duration = System.currentTimeMillis() - startTime;
            logMetrics(operationName, duration);
        }

        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
