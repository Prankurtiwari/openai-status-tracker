package com.statustracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_change_logs", indexes = {
    @Index(name = "idx_service_change", columnList = "service_id,changed_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String serviceId;
    private String serviceName;
    private String previousStatus;
    private String currentStatus;
    private String provider;
    private LocalDateTime changedAt;
}
