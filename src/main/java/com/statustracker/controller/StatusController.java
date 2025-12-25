package com.statustracker.controller;

import com.statustracker.entity.IncidentLog;
import com.statustracker.service.StatusPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController {
    
    @Autowired
    private StatusPageService statusPageService;

    @GetMapping("/active")
    public ResponseEntity<List<IncidentLog>> getActiveIncidents() {
        return ResponseEntity.ok(statusPageService.getActiveIncidents());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<IncidentLog>> getRecentIncidents(
            @RequestParam(defaultValue = "openai") String provider,
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(statusPageService.getRecentIncidents(provider, hours));
    }
}
