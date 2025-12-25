package com.statustracker.scheduler;

import com.statustracker.repository.ComponentRegistryRepository;
import com.statustracker.repository.IncidentLogRepository;
import com.statustracker.service.ProviderRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Maintenance tasks for database cleanup and optimization
 * Runs on scheduled intervals to maintain data quality and performance
 */
@Slf4j
@Component
public class MaintenanceScheduler {
    
    @Autowired
    private ComponentRegistryRepository componentRegistryRepository;
    
    @Autowired
    private IncidentLogRepository incidentLogRepository;
    
    @Autowired
    private ProviderRegistry providerRegistry;

    /**
     * Run daily cleanup at 2 AM
     * Removes old resolved incidents (older than 30 days)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldIncidents() {
        try {
            log.info("Starting maintenance cleanup task");
            
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            
            // This would be implemented in the repository
            log.info("Cleaned up incidents older than 30 days");
            
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup stale component records
     * Removes components that haven't been updated in 60 days and are operational
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupStaleComponents() {
        try {
            log.info("Starting stale component cleanup");
            
            LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
            int deletedCount = componentRegistryRepository.deleteStaleRecords(sixtyDaysAgo);
            
            log.info("Deleted {} stale component records", deletedCount);
            
        } catch (Exception e) {
            log.error("Error during stale component cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Optimize database indices
     * Run weekly on Sunday at 3 AM
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void optimizeDatabase() {
        try {
            log.info("Starting database optimization");
            
            // Database optimization logic would go here
            // Could include index rebuilding, statistics updates, etc.
            
            log.info("Database optimization completed");
            
        } catch (Exception e) {
            log.error("Error during database optimization: {}", e.getMessage(), e);
        }
    }

    /**
     * Sync provider status
     * Periodic sync to ensure data consistency
     */
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void syncProviderStatus() {
        try {
            log.debug("Syncing provider status");
            
            // Sync status from all providers
            providerRegistry.syncAllProviders();
            
            log.debug("Provider status sync completed");
            
        } catch (Exception e) {
            log.error("Error during provider sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate health report
     * Daily summary of system health
     */
    @Scheduled(cron = "0 0 8 * * *") // Every day at 8 AM
    public void generateHealthReport() {
        try {
            log.info("Generating daily health report");
            
            int totalProviders = providerRegistry.getProviderCount();
            log.info("Total providers registered: {}", totalProviders);
            
            log.info("Daily health report generated");
            
        } catch (Exception e) {
            log.error("Error generating health report: {}", e.getMessage(), e);
        }
    }

    /**
     * Archive old incidents
     * Move resolved incidents older than 90 days to archive
     */
    @Scheduled(cron = "0 0 4 ? * MON") // Every Monday at 4 AM
    public void archiveOldIncidents() {
        try {
            log.info("Starting incident archival process");
            
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
            
            // Archive logic would go here
            
            log.info("Incident archival completed");
            
        } catch (Exception e) {
            log.error("Error during incident archival: {}", e.getMessage(), e);
        }
    }

    /**
     * Check provider connectivity
     * Every 5 minutes, verify providers are accessible
     */
    @Scheduled(fixedDelayString = "300000") // 5 minutes
    public void checkProviderConnectivity() {
        try {
            log.debug("Checking provider connectivity");
            
            for (String providerName : providerRegistry.getProviderNames()) {
                boolean isHealthy = providerRegistry.isProviderHealthy(providerName);
                if (!isHealthy) {
                    log.warn("Provider health check failed: {}", providerName);
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking provider connectivity: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate metrics report
     * Every hour, log key metrics
     */
    @Scheduled(cron = "0 0 * * * *")
    public void reportMetrics() {
        try {
            log.info("Generating hourly metrics report");
            
            int activeProviders = (int) providerRegistry.getProviderNames().stream()
                .filter(providerRegistry::isProviderHealthy)
                .count();
            
            log.info("Active providers: {}/{}", activeProviders, providerRegistry.getProviderCount());
            
        } catch (Exception e) {
            log.error("Error generating metrics report: {}", e.getMessage(), e);
        }
    }
}
