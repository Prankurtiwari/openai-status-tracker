package com.statustracker.scheduler;

import com.statustracker.service.StatusPageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polling Scheduler
 * ✅ FIXED: Calls StatusPageService.syncIncidentsFromPolling instead of PollingService directly
 */
@Slf4j
@Component
public class PollingScheduler {

    @Autowired
    private StatusPageService statusPageService;

    @Value("${app.polling.enabled}")
    private boolean pollingEnabled;

    /**
     * Fallback polling every 5 minutes if webhooks are not available
     */
    @Scheduled(
            initialDelayString = "${app.polling.initialDelaySeconds}000",
            fixedDelayString = "${app.polling.intervalSeconds}000"
    )
    public void scheduleStatusPolling() {
        if (!pollingEnabled) {
            return;
        }

        try {
            log.debug("Executing scheduled polling task");
            // ✅ FIXED: Call through StatusPageService instead of PollingService directly
            statusPageService.syncIncidentsFromPolling("openai");
        } catch (Exception e) {
            log.error("Polling scheduler error: {}", e.getMessage(), e);
        }
    }
}
