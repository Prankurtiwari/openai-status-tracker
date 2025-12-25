package com.statustracker.controller;

import com.statustracker.service.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {
    
    @Autowired
    private WebhookService webhookService;

    /**
     * OpenAI Status Page Webhook Endpoint
     * Receives incident and component updates
     */
    @PostMapping("/openai")
    public ResponseEntity<?> handleOpenAiWebhook(@RequestBody String payload) {
        log.info("Received webhook from OpenAI Status Page");
        
        try {
            webhookService.handleStatusPageWebhook(payload, "openai");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Webhook processed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Generic webhook endpoint for other providers
     */
    @PostMapping("/{provider}")
    public ResponseEntity<?> handleGenericWebhook(
            @PathVariable String provider,
            @RequestBody String payload) {
        log.info("Received webhook from provider: {}", provider);
        
        try {
            webhookService.handleStatusPageWebhook(payload, provider);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
