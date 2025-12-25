package com.statustracker.provider;

import com.statustracker.model.ComponentStatus;
import com.statustracker.model.StatusUpdate;

import java.util.List;

/**
 * Interface for status page providers
 * Allows pluggable support for different status page platforms
 * Can be extended for Atlassian Statuspage, Status.io, custom solutions, etc.
 */
public interface StatusPageProvider {
    
    /**
     * Get provider name
     */
    String getProviderName();
    
    /**
     * Get provider's page ID
     */
    String getPageId();
    
    /**
     * Get all current incidents
     */
    List<StatusUpdate> getIncidents();
    
    /**
     * Get component status
     */
    List<ComponentStatus> getComponents();
    
    /**
     * Fetch latest status updates from provider
     * This is called by polling mechanism
     */
    void syncStatus();
    
    /**
     * Check if provider is healthy and accessible
     */
    boolean isHealthy();
    
    /**
     * Validate webhook signature (provider-specific)
     */
    boolean validateWebhookSignature(String payload, String signature);
    
    /**
     * Get last sync time
     */
    long getLastSyncTime();
    
    /**
     * Get webhook URL for this provider
     */
    String getWebhookUrl();
}
