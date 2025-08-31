package com.onion.emailnotificationservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onion.emailnotificationservice.entity.Notification;
import com.onion.emailnotificationservice.service.NasaApodService.ApodResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailContentBuilder {

    private final NasaApodService nasaApodService;

    public EmailContentBuilder(NasaApodService nasaApodService) {
        this.nasaApodService = nasaApodService;
    }

    public String buildEmailContent(List<Notification> notifications, String userName) {
        if (notifications == null || notifications.isEmpty()) {
            return null;
        }

        try {
            // Build asteroid content
            String asteroidContent = buildAsteroidContent(notifications);
            
            // Build APOD content
            String apodContent = buildApodContent();
            
            // Create the complete email HTML
            return EmailTemplate.createEmailHtml(asteroidContent, apodContent, java.time.LocalDateTime.now(), userName);
            
        } catch (Exception e) {
            log.error("Critical error building email content: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Backward compatibility method
    public String buildEmailContent(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return null;
        }

        try {
            // Build asteroid content
            String asteroidContent = buildAsteroidContent(notifications);
            
            // Build APOD content
            String apodContent = buildApodContent();
            
            // Create the complete email HTML with default greeting
            return EmailTemplate.createEmailHtml(asteroidContent, apodContent, java.time.LocalDateTime.now(), null);
            
        } catch (Exception e) {
            log.error("Critical error building email content: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildAsteroidContent(List<Notification> notifications) {
        StringBuilder asteroidContent = new StringBuilder();
        
        notifications.forEach(notification -> {
            try {
                String asteroidCard = EmailTemplate.createAsteroidCardHtml(
                    notification.getAsteroidName(),
                    notification.getCloseApproachDate().toString(),
                    notification.getEstimatedDiameterAvgMeters(),
                    notification.getMissDistanceKilometers().doubleValue(),
                    EmailTemplate.getRiskLevelHtml(notification.getMissDistanceKilometers().doubleValue())
                );
                asteroidContent.append(asteroidCard);
            } catch (Exception e) {
                log.error("Error processing notification {} for HTML generation: {}", 
                        notification.getId(), e.getMessage(), e);
            }
        });
        
        return asteroidContent.toString();
    }

    private String buildApodContent() {
        try {
            ApodResponse apod = nasaApodService.getApodForToday();
            if (apod != null && "image".equals(apod.getMediaType())) {
                log.info("Successfully fetched APOD: {}", apod.getTitle());
                return EmailTemplate.createApodSectionHtml(
                    apod.getTitle(),
                    apod.getUrl(),
                    apod.getExplanation(),
                    apod.getDate(),
                    apod.getCopyright()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to fetch APOD, continuing without it: {}", e.getMessage());
            // Continue without APOD - it's not critical
        }
        
        return null;
    }
}
