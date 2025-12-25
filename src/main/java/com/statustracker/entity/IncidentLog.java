package com.statustracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_logs", indexes = {
    @Index(name = "idx_provider_timestamp", columnList = "provider,created_at"),
    @Index(name = "idx_service_status", columnList = "service_name,status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String incidentId;
    private String provider;
    private String serviceName;
    private String status;
    private String statusMessage;
    private String severity;
    
    @Lob
    private String affectedComponents;
    
    private String incidentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    
    @Column(name = "hash_code")
    private String hashCode;
    
    private Boolean isNotified = false;
}
