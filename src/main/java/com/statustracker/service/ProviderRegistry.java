package com.statustracker.service;

import com.statustracker.provider.OpenAiStatusProvider;
import com.statustracker.provider.StatusPageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Registry for managing multiple status page providers
 * Allows dynamic addition/removal of providers
 * Supports scaling to 100+ status pages
 */
@Slf4j
@Service
public class ProviderRegistry {
    
    private final Map<String, StatusPageProvider> providers = new HashMap<>();
    
    @Autowired
    private OpenAiStatusProvider openAiProvider;
    
    @Autowired(required = false)
    private List<StatusPageProvider> customProviders;

    /**
     * Initialize provider registry with default and custom providers
     */
    public void init() {
        log.info("Initializing Provider Registry");
        
        // Register default providers
        registerProvider("openai", openAiProvider);
        
        // Register custom providers (if any)
        if (customProviders != null) {
            for (StatusPageProvider provider : customProviders) {
                registerProvider(provider.getProviderName(), provider);
            }
        }
        
        log.info("Provider Registry initialized with {} providers", providers.size());
    }

    /**
     * Register a new provider
     */
    public void registerProvider(String providerName, StatusPageProvider provider) {
        if (providerName == null || provider == null) {
            throw new IllegalArgumentException("Provider name and implementation cannot be null");
        }
        
        providers.put(providerName.toLowerCase(), provider);
        log.info("Provider registered: {}", providerName);
    }

    /**
     * Get provider by name
     */
    public StatusPageProvider getProvider(String providerName) {
        StatusPageProvider provider = providers.get(providerName.toLowerCase());
        if (provider == null) {
            log.warn("Provider not found: {}", providerName);
            throw new IllegalArgumentException("Unknown provider: " + providerName);
        }
        return provider;
    }

    /**
     * Check if provider is registered
     */
    public boolean hasProvider(String providerName) {
        return providers.containsKey(providerName.toLowerCase());
    }

    /**
     * Get all registered providers
     */
    public Collection<StatusPageProvider> getAllProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    /**
     * Get provider names
     */
    public Set<String> getProviderNames() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    /**
     * Unregister a provider
     */
    public void unregisterProvider(String providerName) {
        providers.remove(providerName.toLowerCase());
        log.info("Provider unregistered: {}", providerName);
    }

    /**
     * Get total number of registered providers
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * Check provider health
     */
    public boolean isProviderHealthy(String providerName) {
        try {
            StatusPageProvider provider = getProvider(providerName);
            return provider.isHealthy();
        } catch (Exception e) {
            log.error("Provider health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get health status of all providers
     */
    public Map<String, Boolean> getAllProvidersHealth() {
        Map<String, Boolean> healthStatus = new HashMap<>();
        for (String providerName : providers.keySet()) {
            healthStatus.put(providerName, isProviderHealthy(providerName));
        }
        return healthStatus;
    }

    /**
     * Sync status from all providers
     */
    public void syncAllProviders() {
        log.info("Syncing status from all {} providers", providers.size());
        
        for (String providerName : providers.keySet()) {
            try {
                StatusPageProvider provider = getProvider(providerName);
                provider.syncStatus();
                log.debug("Synced status from: {}", providerName);
            } catch (Exception e) {
                log.error("Error syncing status from {}: {}", providerName, e.getMessage());
            }
        }
    }
}
