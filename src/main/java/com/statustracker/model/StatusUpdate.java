package com.statustracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdate {
    private String productName;
    private String serviceId;
    private String status;
    private String statusMessage;
    private LocalDateTime timestamp;
    private String incidentUrl;
    private String provider;
    private String severity;
}
