package com.statustracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    private String id;
    private String name;
    private String status;
    private String impact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private List<String> affectedComponents;
    private List<IncidentUpdate> updates;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class IncidentUpdate {
    private String status;
    private String body;
    private LocalDateTime createdAt;
    private String displayAt;
}
