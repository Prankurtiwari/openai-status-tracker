package com.statustracker.service;

import com.statustracker.model.StatusUpdate;
import com.statustracker.util.DateFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Notification Service for alerting about status changes
 * Supports multiple channels: Console, Email, Slack, Telegram
 * Can be extended for webhooks, SMS, etc.
 */
@Slf4j
@Service
public class NotificationService {
    
    @Value("${app.notification.slack.enabled:false}")
    private boolean slackEnabled;
    
    @Value("${app.notification.slack.webhook-url}")
    private String slackWebhookUrl;
    
    @Value("${app.notification.telegram.enabled:false}")
    private boolean telegramEnabled;
    
    @Value("${app.notification.telegram.token}")
    private String telegramToken;
    
    @Value("${app.notification.telegram.chat-id}")
    private String telegramChatId;
    
    @Value("${app.notification.email.enabled:false}")
    private boolean emailEnabled;
    
    @Autowired(required = false)
    private SlackNotificationClient slackClient;
    
    @Autowired(required = false)
    private TelegramNotificationClient telegramClient;

    /**
     * Notify about new incident
     * Asynchronous to avoid blocking the webhook handler
     */
    @Async
    public void notifyNewIncident(StatusUpdate update) {
        try {
            String message = formatNewIncidentMessage(update);
            
            // Console notification (always enabled)
            notifyConsole(message, "🔴 NEW INCIDENT");
            
            // Multi-channel notifications
            if (slackEnabled && slackClient != null) {
                notifySlack(message, update, "warning");
            }
            
            if (telegramEnabled && telegramClient != null) {
                notifyTelegram(message, update);
            }
            
            if (emailEnabled) {
                notifyEmail(message, update);
            }
            
            log.info("New incident notification sent: {}", update.getProductName());
        } catch (Exception e) {
            log.error("Error sending new incident notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify about status change
     */
    @Async
    public void notifyStatusChange(StatusUpdate update) {
        try {
            String message = formatStatusChangeMessage(update);
            
            notifyConsole(message, "🟡 STATUS CHANGE");
            
            if (slackEnabled && slackClient != null) {
                notifySlack(message, update, "info");
            }
            
            if (telegramEnabled && telegramClient != null) {
                notifyTelegram(message, update);
            }
            
            log.info("Status change notification sent: {}", update.getProductName());
        } catch (Exception e) {
            log.error("Error sending status change notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify about incident resolution
     */
    @Async
    public void notifyIncidentResolved(StatusUpdate update) {
        try {
            String message = formatResolvedMessage(update);
            
            notifyConsole(message, "🟢 RESOLVED");
            
            if (slackEnabled && slackClient != null) {
                notifySlack(message, update, "good");
            }
            
            if (telegramEnabled && telegramClient != null) {
                notifyTelegram(message, update);
            }
            
            log.info("Incident resolved notification sent: {}", update.getProductName());
        } catch (Exception e) {
            log.error("Error sending resolved notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify via console (stdout)
     */
    private void notifyConsole(String message, String prefix) {
        String timestamp = DateFormatter.formatTimestamp(LocalDateTime.now());
        System.out.println(String.format("[%s] %s - %s", timestamp, prefix, message));
    }

    /**
     * Notify via Slack
     */
    private void notifySlack(String message, StatusUpdate update, String color) {
        try {
            if (slackClient != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("text", message);
                payload.put("color", color);
                payload.put("title", update.getProductName());
                payload.put("value", update.getStatusMessage());
                
                slackClient.sendNotification(slackWebhookUrl, payload);
                log.debug("Slack notification sent");
            }
        } catch (Exception e) {
            log.error("Slack notification failed: {}", e.getMessage());
        }
    }

    /**
     * Notify via Telegram
     */
    private void notifyTelegram(String message, StatusUpdate update) {
        try {
            if (telegramClient != null) {
                String formattedMessage = String.format(
                    "🔔 *Status Alert*\n" +
                    "*Product:* %s\n" +
                    "*Status:* %s\n" +
                    "*Message:* %s\n" +
                    "*Provider:* %s\n" +
                    "*Time:* %s",
                    update.getProductName(),
                    update.getStatus().toUpperCase(),
                    update.getStatusMessage(),
                    update.getProvider().toUpperCase(),
                    DateFormatter.formatTimestamp(LocalDateTime.now())
                );
                
                telegramClient.sendMessage(telegramChatId, formattedMessage);
                log.debug("Telegram notification sent");
            }
        } catch (Exception e) {
            log.error("Telegram notification failed: {}", e.getMessage());
        }
    }

    /**
     * Notify via Email
     */
    private void notifyEmail(String message, StatusUpdate update) {
        try {
            log.info("Email notification would be sent here: {}", message);
            // Integration with EmailService would go here
        } catch (Exception e) {
            log.error("Email notification failed: {}", e.getMessage());
        }
    }

    /**
     * Format new incident message
     */
    private String formatNewIncidentMessage(StatusUpdate update) {
        return String.format(
            "Product: %s | Status: %s | Severity: %s | Message: %s",
            update.getProductName(),
            update.getStatus().toUpperCase(),
            update.getSeverity().toUpperCase(),
            update.getStatusMessage()
        );
    }

    /**
     * Format status change message
     */
    private String formatStatusChangeMessage(StatusUpdate update) {
        return String.format(
            "Status Update for %s: %s",
            update.getProductName(),
            update.getStatusMessage()
        );
    }

    /**
     * Format resolved message
     */
    private String formatResolvedMessage(StatusUpdate update) {
        return String.format(
            "✅ Incident Resolved: %s | Resolution: %s",
            update.getProductName(),
            update.getStatusMessage()
        );
    }
}

/**
 * Slack notification client (stub - implement with RestTemplate)
 */
@Slf4j
class SlackNotificationClient {
    public void sendNotification(String webhookUrl, Map<String, Object> payload) {
        // Implementation would use RestTemplate or WebClient
        log.debug("Slack payload: {}", payload);
    }
}

/**
 * Telegram notification client (stub - implement with RestTemplate)
 */
@Slf4j
class TelegramNotificationClient {
    public void sendMessage(String chatId, String message) {
        // Implementation would use RestTemplate or WebClient
        log.debug("Telegram message to {}: {}", chatId, message);
    }
}
