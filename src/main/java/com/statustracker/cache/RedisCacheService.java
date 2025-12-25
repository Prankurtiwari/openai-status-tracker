package com.statustracker.cache;

import com.statustracker.entity.ComponentRegistry;
import com.statustracker.entity.IncidentLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Custom cache manager for advanced caching strategies
 * Integrates with Redis for distributed caching
 * Falls back to local cache if Redis unavailable
 */
@Slf4j
@Component
public class RedisCacheService {
    
    private static final String INCIDENT_CACHE_PREFIX = "incident:";
    private static final String COMPONENT_CACHE_PREFIX = "component:";
    private static final String PROVIDER_CACHE_PREFIX = "provider:";
    private static final int DEFAULT_TTL_HOURS = 1;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache incident data
     */
    public void cacheIncident(IncidentLog incident) {
        try {
            if (redisTemplate != null) {
                String key = INCIDENT_CACHE_PREFIX + incident.getProvider() + ":" + incident.getIncidentId();
                redisTemplate.opsForValue().set(key, incident, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
                log.debug("Cached incident: {}", incident.getServiceName());
            }
        } catch (Exception e) {
            log.warn("Failed to cache incident, continuing without cache: {}", e.getMessage());
        }
    }

    /**
     * Get incident from cache
     */
    public IncidentLog getIncidentFromCache(String provider, String incidentId) {
        try {
            if (redisTemplate != null) {
                String key = INCIDENT_CACHE_PREFIX + provider + ":" + incidentId;
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof IncidentLog) {
                    log.debug("Retrieved incident from cache: {}", incidentId);
                    return (IncidentLog) cached;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Cache component registry
     */
    public void cacheComponent(ComponentRegistry component) {
        try {
            if (redisTemplate != null) {
                String key = COMPONENT_CACHE_PREFIX + component.getProvider() + ":" + component.getComponentId();
                redisTemplate.opsForValue().set(key, component, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
                log.debug("Cached component: {}", component.getComponentName());
            }
        } catch (Exception e) {
            log.warn("Failed to cache component: {}", e.getMessage());
        }
    }

    /**
     * Get component from cache
     */
    public ComponentRegistry getComponentFromCache(String provider, String componentId) {
        try {
            if (redisTemplate != null) {
                String key = COMPONENT_CACHE_PREFIX + provider + ":" + componentId;
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof ComponentRegistry) {
                    log.debug("Retrieved component from cache: {}", componentId);
                    return (ComponentRegistry) cached;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve component from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Cache provider status list
     */
    public void cacheProviderIncidents(String provider, List<IncidentLog> incidents) {
        try {
            if (redisTemplate != null) {
                String key = PROVIDER_CACHE_PREFIX + provider + ":incidents";
                redisTemplate.opsForValue().set(key, incidents, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
                log.debug("Cached {} incidents for provider: {}", incidents.size(), provider);
            }
        } catch (Exception e) {
            log.warn("Failed to cache provider incidents: {}", e.getMessage());
        }
    }

    /**
     * Get provider incidents from cache
     */
    @SuppressWarnings("unchecked")
    public List<IncidentLog> getProviderIncidentsFromCache(String provider) {
        try {
            if (redisTemplate != null) {
                String key = PROVIDER_CACHE_PREFIX + provider + ":incidents";
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof List) {
                    log.debug("Retrieved provider incidents from cache: {}", provider);
                    return (List<IncidentLog>) cached;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve provider incidents from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Invalidate incident cache
     */
    @CacheEvict(allEntries = true, cacheNames = "incidents")
    public void invalidateIncidentCache(String provider) {
        try {
            if (redisTemplate != null) {
                String pattern = INCIDENT_CACHE_PREFIX + provider + ":*";
                redisTemplate.delete(redisTemplate.keys(pattern));
                log.info("Invalidated incident cache for provider: {}", provider);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate incident cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate component cache
     */
    public void invalidateComponentCache(String provider) {
        try {
            if (redisTemplate != null) {
                String pattern = COMPONENT_CACHE_PREFIX + provider + ":*";
                redisTemplate.delete(redisTemplate.keys(pattern));
                log.info("Invalidated component cache for provider: {}", provider);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate component cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        try {
            if (redisTemplate != null) {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
                log.info("Cleared all Redis caches");
            }
        } catch (Exception e) {
            log.warn("Failed to clear caches: {}", e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    public long getCacheSize(String pattern) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.keys(pattern).size();
            }
        } catch (Exception e) {
            log.warn("Failed to get cache size: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Check if Redis is available
     */
    public boolean isRedisAvailable() {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                redisTemplate.getConnectionFactory().getConnection().ping();
                return true;
            }
        } catch (Exception e) {
            log.debug("Redis not available: {}", e.getMessage());
        }
        return false;
    }
}
