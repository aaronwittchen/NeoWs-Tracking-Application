package com.onion.emailnotificationservice.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.onion.emailnotificationservice.entity.Notification;
import com.onion.emailnotificationservice.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationProcessor {

    private final NotificationRepository notificationRepository;

    public NotificationProcessor(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUnsentNotifications() {
        return notificationRepository.findByEmailSent(false);
    }

    public void markNotificationsAsSent(List<Notification> notifications) {
        try {
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            notifications.forEach(notification -> {
                try {
                    notification.setEmailSent(true);
                    notificationRepository.save(notification);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    log.error("Failed to mark notification {} as sent: {}", 
                            notification.getId(), e.getMessage(), e);
                }
            });
            
            log.info("Notification marking completed: {} successful, {} failed", 
                    successCount.get(), failureCount.get());
                    
        } catch (Exception e) {
            log.error("Critical error marking notifications as sent: {}", e.getMessage(), e);
        }
    }

    public boolean hasUnsentNotifications() {
        List<Notification> notifications = getUnsentNotifications();
        return !notifications.isEmpty();
    }
}
