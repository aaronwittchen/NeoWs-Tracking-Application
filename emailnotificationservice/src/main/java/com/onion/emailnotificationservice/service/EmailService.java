package com.onion.emailnotificationservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.onion.emailnotificationservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final UserRepository userRepository;
    private final NotificationProcessor notificationProcessor;
    private final EmailContentBuilder emailContentBuilder;
    private final EmailSenderService emailSenderService;

    public EmailService(UserRepository userRepository,
                        NotificationProcessor notificationProcessor,
                        EmailContentBuilder emailContentBuilder,
                        EmailSenderService emailSenderService) {
        this.userRepository = userRepository;
        this.notificationProcessor = notificationProcessor;
        this.emailContentBuilder = emailContentBuilder;
        this.emailSenderService = emailSenderService;
    }

    @Async
    public void sendAsteroidAlertEmail() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting asteroid alert email process at {}", startTime);

        try {
            // Step 1: Check if there are notifications to send
            if (!notificationProcessor.hasUnsentNotifications()) {
                log.info("No asteroid alerts to send at {}", LocalDateTime.now());
                return;
            }

            // Step 2: Get users with notifications enabled
            List<com.onion.emailnotificationservice.entity.User> usersWithNotifications = userRepository.findAll().stream()
                .filter(user -> user.isNotificationEnabled())
                .toList();
            
            if (usersWithNotifications.isEmpty()) {
                log.info("No users with email notifications enabled at {}", LocalDateTime.now());
                return;
            }
            log.info("Found {} users with notifications enabled", usersWithNotifications.size());

            // Step 3: Get notifications
            List<com.onion.emailnotificationservice.entity.Notification> notifications = 
                notificationProcessor.getUnsentNotifications();
            
            if (notifications == null || notifications.isEmpty()) {
                log.warn("No notifications to send");
                return;
            }

            // Step 4: Send personalized emails to each user
            log.info("Preparing to send {} notifications to {} users", notifications.size(), usersWithNotifications.size());
            
            int successfulEmails = 0;
            int failedEmails = 0;
            
            for (com.onion.emailnotificationservice.entity.User user : usersWithNotifications) {
                try {
                    // Build personalized email content for each user
                    String personalizedHtmlContent = emailContentBuilder.buildEmailContent(notifications, user.getFullName());
                    
                    if (personalizedHtmlContent != null) {
                        // Send email to individual user
                        emailSenderService.sendHtmlEmail(user.getEmail(), "NASA Space Watch - Weekly Asteroid Alert", personalizedHtmlContent);
                        successfulEmails++;
                        log.debug("Successfully sent personalized email to user: {}", user.getEmail());
                    } else {
                        log.warn("Failed to build personalized email content for user: {}", user.getEmail());
                        failedEmails++;
                    }
                } catch (Exception e) {
                    log.error("Failed to send email to user {}: {}", user.getEmail(), e.getMessage());
                    failedEmails++;
                }
            }
            
            EmailSenderService.EmailSendResult result = new EmailSenderService.EmailSendResult(successfulEmails, failedEmails);
            
            log.info("Email sending completed: {} successful, {} failed", 
                    result.getSuccessfulCount(), result.getFailedCount());

            // Step 5: Mark notifications as sent only if at least one email was successful
            if (result.hasSuccessfulEmails()) {
                notificationProcessor.markNotificationsAsSent(notifications);
                log.info("Marked {} notifications as sent after successful email delivery", notifications.size());
            } else {
                log.error("No emails were sent successfully. Notifications will remain unsent for retry.");
            }

            LocalDateTime endTime = LocalDateTime.now();
            log.info("Asteroid alert email process completed at {} (Duration: {} seconds)", 
                    endTime, java.time.Duration.between(startTime, endTime).getSeconds());

        } catch (Exception e) {
            log.error("Critical error in sendAsteroidAlertEmail: {}", e.getMessage(), e);
            // Don't mark notifications as sent if there's a critical error
        }
    }

    @Async
    public void sendWelcomeEmailAsync(String email, String fullName) {
        String subject = "Welcome to NASA Space Watch!";
        String htmlContent = "<html><body>"
            + "<h2>Welcome, " + (fullName != null ? fullName : "Space Enthusiast") + "!</h2>"
            + "<p>Thank you for registering for NASA Space Watch asteroid alerts. "
            + "You are now subscribed to receive notifications about near-Earth objects and space events.</p>"
            + "<p>We are excited to have you on board!</p>"
            + "<br><p><em>- The NASA Space Watch Team</em></p>"
            + "</body></html>";
        try {
            emailSenderService.sendHtmlEmail(email, subject, htmlContent);
            log.info("Welcome email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }
}
