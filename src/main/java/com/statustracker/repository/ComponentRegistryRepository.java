package com.statustracker.repository;

import com.statustracker.entity.ComponentRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing component registry data
 */
@Repository
public interface ComponentRegistryRepository extends JpaRepository<ComponentRegistry, Long> {
    
    /**
     * Find component by ID and provider
     */
    Optional<ComponentRegistry> findByComponentIdAndProvider(String componentId, String provider);
    
    /**
     * Find all components for a provider
     */
    List<ComponentRegistry> findByProviderOrderByPositionAsc(String provider);
    
    /**
     * Find all degraded components
     */
    @Query("SELECT c FROM ComponentRegistry c WHERE c.currentStatus != 'operational' " +
           "AND c.currentStatus != 'under_maintenance' ORDER BY c.updatedAt DESC")
    List<ComponentRegistry> findDegradedComponents();
    
    /**
     * Find degraded components for specific provider
     */
    @Query("SELECT c FROM ComponentRegistry c WHERE c.provider = ?1 " +
           "AND c.currentStatus != 'operational' AND c.currentStatus != 'under_maintenance'")
    List<ComponentRegistry> findDegradedComponentsByProvider(String provider);
    
    /**
     * Find critical components
     */
    @Query("SELECT c FROM ComponentRegistry c WHERE c.impactLevel = 'critical' " +
           "AND c.currentStatus != 'operational'")
    List<ComponentRegistry> findCriticalComponents();
    
    /**
     * Find recently checked components
     */
    @Query("SELECT c FROM ComponentRegistry c WHERE c.lastCheckedAt >= ?1 " +
           "ORDER BY c.lastCheckedAt DESC")
    List<ComponentRegistry> findRecentlyChecked(LocalDateTime since);
    
    /**
     * Find components that haven't been checked recently
     */
    @Query("SELECT c FROM ComponentRegistry c WHERE c.lastCheckedAt IS NULL " +
           "OR c.lastCheckedAt < ?1 ORDER BY c.lastCheckedAt ASC")
    List<ComponentRegistry> findStaleComponents(LocalDateTime since);
    
    /**
     * Count degraded components by provider
     */
    @Query("SELECT COUNT(c) FROM ComponentRegistry c WHERE c.provider = ?1 " +
           "AND c.currentStatus != 'operational' AND c.currentStatus != 'under_maintenance'")
    long countDegradedByProvider(String provider);
    
    /**
     * Find components in a group
     */
    List<ComponentRegistry> findByProviderAndGroupId(String provider, String groupId);
    
    /**
     * Find all components updated after a specific time
     */
    List<ComponentRegistry> findByUpdatedAtAfter(LocalDateTime dateTime);
    
    /**
     * Delete old component records (for maintenance)
     */
    @Query("DELETE FROM ComponentRegistry c WHERE c.updatedAt < ?1 AND c.currentStatus = 'operational'")
    int deleteStaleRecords(LocalDateTime before);
}
