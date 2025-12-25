package com.statustracker.repository;

import com.statustracker.entity.StatusChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatusChangeLogRepository extends JpaRepository<StatusChangeLog, Long> {
    List<StatusChangeLog> findByServiceIdAndChangedAtAfter(String serviceId, LocalDateTime dateTime);
    List<StatusChangeLog> findByProviderOrderByChangedAtDesc(String provider);
}
