package com.statustracker.repository;

import com.statustracker.entity.IncidentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentLogRepository extends JpaRepository<IncidentLog, Long> {
    
    Optional<IncidentLog> findByIncidentIdAndProvider(String incidentId, String provider);
    
    List<IncidentLog> findByProviderAndCreatedAtAfter(String provider, LocalDateTime dateTime);
    
    @Query("SELECT i FROM IncidentLog i WHERE i.status != 'resolved' ORDER BY i.updatedAt DESC")
    List<IncidentLog> findActiveIncidents();
    
    @Query("SELECT DISTINCT i.serviceName FROM IncidentLog i WHERE i.provider = ?1")
    List<String> findAffectedServices(String provider);
}
