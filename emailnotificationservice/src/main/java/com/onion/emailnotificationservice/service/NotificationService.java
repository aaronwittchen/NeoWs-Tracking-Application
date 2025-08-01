package com.onion.emailnotificationservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.onion.NeoWs.event.AsteroidCollisionEvent;
import com.onion.emailnotificationservice.entity.Notification;
import com.onion.emailnotificationservice.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }
    
    @KafkaListener(topics = "asteroid-alert", groupId = "notification-service")
    public void alertEvent(AsteroidCollisionEvent notificationEvent) {
        log.info("Received asteroid collision alert: {}", notificationEvent);

        try {
            // Validate the event data
            if (notificationEvent == null) {
                log.error("Received null asteroid collision event");
                return;
            }

            if (notificationEvent.getAsteroidName() == null || notificationEvent.getAsteroidName().trim().isEmpty()) {
                log.error("Received asteroid event with null or empty asteroid name");
                return;
            }

            if (notificationEvent.getCloseApproachDate() == null || notificationEvent.getCloseApproachDate().trim().isEmpty()) {
                log.error("Received asteroid event with null or empty close approach date");
                return;
            }

            // Create entity for notification
            final Notification notification = Notification.builder()
                    .asteroidName(notificationEvent.getAsteroidName())
                    .closeApproachDate(LocalDate.parse(notificationEvent.getCloseApproachDate()))
                    .estimatedDiameterAvgMeters(notificationEvent.getEstimatedDiameterAverageMeters())
                    .missDistanceKilometers(new BigDecimal(notificationEvent.getMissDistanceKilometers()))
                    .emailSent(false)
                    .build();

            // Save notification
            final Notification savedNotification = notificationRepository.saveAndFlush(notification);
            log.info("Notification saved successfully: {}", savedNotification);

        } catch (NumberFormatException e) {
            log.error("Failed to parse numeric values from asteroid event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to process asteroid collision event: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void sendAlertingEmail() {
        try {
            log.info("Triggering scheduled job to send email alerts at {}", LocalDateTime.now());
            emailService.sendAsteroidAlertEmail();
        } catch (Exception e) {
            log.error("Error in scheduled email job: {}", e.getMessage(), e);
        }
    }
}
