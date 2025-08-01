package com.onion.emailnotificationservice.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.onion.emailnotificationservice.entity.Notification;
import com.onion.emailnotificationservice.repository.NotificationRepository;
import com.onion.emailnotificationservice.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Value("${email.service.from.email}")
    private String fromEmail;

     private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final NasaApodService nasaApodService;

    public EmailService(NotificationRepository notificationRepository,
                        UserRepository userRepository,
                        JavaMailSender mailSender,
                        NasaApodService nasaApodService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.nasaApodService = nasaApodService;
    }

    @Async
    public void sendAsteroidAlertEmail() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Starting asteroid alert email process at {}", startTime);

        try {
            // Step 1: Create HTML content
            final String htmlContent = createEmailHtml();
            if(htmlContent == null) {
                log.info("No asteroid alerts to send at {}", LocalDateTime.now());
                return;
            }

            // Step 2: Get users with notifications enabled
            final List<String> toEmails = userRepository.findAllEmailsAndNotificationEnabled();
            log.info("Found {} users with notifications enabled: {}", toEmails.size(), toEmails);
            
            if (toEmails.isEmpty()) {
                log.info("No users with email notifications enabled at {}", LocalDateTime.now());
                return;
            }

            // Step 3: Get list of notifications to send
            List<Notification> notificationList = notificationRepository.findByEmailSent(false);
            if (notificationList.isEmpty()) {
                log.warn("No unsent notifications found, but users exist. This might indicate a data inconsistency.");
                return;
            }

            log.info("Preparing to send {} notifications to {} users", notificationList.size(), toEmails.size());
            
            // Step 4: Send emails to all users with improved error handling
            AtomicInteger successfulEmails = new AtomicInteger(0);
            AtomicInteger failedEmails = new AtomicInteger(0);
            
            List<CompletableFuture<Boolean>> emailFutures = toEmails.stream()
                .map(toEmail -> CompletableFuture.supplyAsync(() -> sendHtmlEmail(toEmail, htmlContent)))
                .toList();

            // Wait for all emails to complete
            CompletableFuture.allOf(emailFutures.toArray(new CompletableFuture[0])).join();

            // Count results
            emailFutures.forEach(future -> {
                try {
                    if (future.get()) {
                        successfulEmails.incrementAndGet();
                    } else {
                        failedEmails.incrementAndGet();
                    }
                } catch (Exception e) {
                    failedEmails.incrementAndGet();
                    log.error("Error processing email future: {}", e.getMessage(), e);
                }
            });

            log.info("Email sending completed: {} successful, {} failed", successfulEmails.get(), failedEmails.get());

            // Step 5: Mark notifications as sent only if at least one email was successful
            if (successfulEmails.get() > 0) {
                markNotificationsAsSent(notificationList);
                log.info("Marked {} notifications as sent after successful email delivery", notificationList.size());
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

    private boolean sendHtmlEmail(final String toEmail, final String htmlContent) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setTo(toEmail);
                helper.setFrom(fromEmail);
                helper.setSubject("🚨 NASA Asteroid Alert - Close Approach Detected");
                helper.setText(htmlContent, true); // true indicates HTML content
                
                mailSender.send(message);
                log.info("HTML email sent successfully to: {} (attempt {})", toEmail, retryCount + 1);
                return true;
                
            } catch (MessagingException e) {
                retryCount++;
                log.warn("Failed to send HTML email to {} (attempt {}/{}): {}", 
                        toEmail, retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    log.error("Failed to send HTML email to {} after {} attempts: {}", 
                            toEmail, maxRetries, e.getMessage(), e);
                    return false;
                }
                
                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep(1000 * retryCount); // 1s, 2s, 3s delays
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting for email retry to {}", toEmail);
                    return false;
                }
            } catch (Exception e) {
                log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    private void markNotificationsAsSent(List<Notification> notifications) {
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

    private String createEmailHtml() {
        try {
            // check if there are any asteroids to send alerts for
            List<Notification> notificationList = notificationRepository.findByEmailSent(false);

            if (notificationList.isEmpty()) {
                return null;
            }

            // Fetch today's APOD with error handling
            NasaApodService.ApodResponse apod = null;
            try {
                apod = nasaApodService.getApodForToday();
                if (apod != null) {
                    log.info("Successfully fetched APOD: {}", apod.getTitle());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch APOD, continuing without it: {}", e.getMessage());
                // Continue without APOD - it's not critical
            }

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>");
            htmlContent.append("<html lang=\"en\">");
            htmlContent.append("<head>");
            htmlContent.append("<meta charset=\"UTF-8\">");
            htmlContent.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            htmlContent.append("<title>Asteroid Alert</title>");
            htmlContent.append("<style>");
            htmlContent.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }");
            htmlContent.append(".container { max-width: 800px; margin: 0 auto; background-color: #ffffff; }");
            htmlContent.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }");
            htmlContent.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
            htmlContent.append(".header p { margin: 10px 0 0 0; opacity: 0.9; }");
            htmlContent.append(".content { padding: 30px; }");
            htmlContent.append(".alert-box { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin-bottom: 20px; }");
            htmlContent.append(".asteroid-card { background-color: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 15px; border-left: 4px solid #007bff; }");
            htmlContent.append(".asteroid-name { font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px; }");
            htmlContent.append(".asteroid-details { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 15px; }");
            htmlContent.append(".detail-item { background-color: white; padding: 12px; border-radius: 6px; border: 1px solid #e9ecef; }");
            htmlContent.append(".detail-label { font-weight: bold; color: #6c757d; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }");
            htmlContent.append(".detail-value { color: #2c3e50; font-size: 14px; margin-top: 4px; }");
            htmlContent.append(".apod-section { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; margin: 30px 0; text-align: center; }");
            htmlContent.append(".apod-title { font-size: 24px; font-weight: bold; margin-bottom: 15px; }");
            htmlContent.append(".apod-image { max-width: 100%; height: auto; border-radius: 8px; margin: 20px 0; box-shadow: 0 4px 8px rgba(0,0,0,0.3); }");
            htmlContent.append(".apod-explanation { text-align: left; line-height: 1.6; margin: 20px 0; }");
            htmlContent.append(".apod-meta { font-size: 12px; opacity: 0.8; margin-top: 15px; }");
            htmlContent.append(".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 12px; }");
            htmlContent.append(".danger { color: #dc3545; }");
            htmlContent.append(".warning { color: #ffc107; }");
            htmlContent.append(".info { color: #17a2b8; }");
            htmlContent.append("@media (max-width: 768px) {");
            htmlContent.append("  .container { max-width: 95%; }");
            htmlContent.append("  .asteroid-details { grid-template-columns: 1fr 1fr; }");
            htmlContent.append("  .content { padding: 20px; }");
            htmlContent.append("}");
            htmlContent.append("@media (max-width: 480px) {");
            htmlContent.append("  .asteroid-details { grid-template-columns: 1fr; }");
            htmlContent.append("  .content { padding: 15px; }");
            htmlContent.append("}");
            htmlContent.append("</style>");
            htmlContent.append("</head>");
            htmlContent.append("<body>");
            htmlContent.append("<div class=\"container\">");
            htmlContent.append("<div class=\"header\">");
            htmlContent.append("<h1>🚨 Asteroid Alert</h1>");
            htmlContent.append("<p>NASA has detected close approaches by potentially hazardous asteroids</p>");
            htmlContent.append("</div>");
            htmlContent.append("<div class=\"content\">");
            htmlContent.append("<div class=\"alert-box\">");
            htmlContent.append("<strong>⚠️ Important:</strong> The following asteroids have been detected approaching Earth. While these are close approaches, NASA continues to monitor their trajectories.");
            htmlContent.append("</div>");

            notificationList.forEach(notification -> {
                try {
                    htmlContent.append("<div class=\"asteroid-card\">");
                    htmlContent.append("<div class=\"asteroid-name\">🌌 ").append(escapeHtml(notification.getAsteroidName())).append("</div>");
                    htmlContent.append("<div class=\"asteroid-details\">");
                    htmlContent.append("<div class=\"detail-item\">");
                    htmlContent.append("<div class=\"detail-label\">Close Approach Date</div>");
                    htmlContent.append("<div class=\"detail-value\">").append(escapeHtml(notification.getCloseApproachDate().toString())).append("</div>");
                    htmlContent.append("</div>");
                    htmlContent.append("<div class=\"detail-item\">");
                    htmlContent.append("<div class=\"detail-label\">Estimated Diameter</div>");
                    htmlContent.append("<div class=\"detail-value\">").append(String.format("%.2f", notification.getEstimatedDiameterAvgMeters())).append(" meters</div>");
                    htmlContent.append("</div>");
                    htmlContent.append("<div class=\"detail-item\">");
                    htmlContent.append("<div class=\"detail-label\">Miss Distance</div>");
                    htmlContent.append("<div class=\"detail-value\">").append(String.format("%.2f", notification.getMissDistanceKilometers())).append(" km</div>");
                    htmlContent.append("</div>");
                    htmlContent.append("<div class=\"detail-item\">");
                    htmlContent.append("<div class=\"detail-label\">Risk Level</div>");
                    htmlContent.append("<div class=\"detail-value\">").append(getRiskLevel(notification.getMissDistanceKilometers())).append("</div>");
                    htmlContent.append("</div>");
                    htmlContent.append("</div>");
                    htmlContent.append("</div>");
                } catch (Exception e) {
                    log.error("Error processing notification {} for HTML generation: {}", 
                            notification.getId(), e.getMessage(), e);
                }
            });

            // Add APOD section if available
            if (apod != null && "image".equals(apod.getMediaType())) {
                try {
                    htmlContent.append("<div class=\"apod-section\">");
                    htmlContent.append("<div class=\"apod-title\">📸 Astronomy Picture of the Day</div>");
                    htmlContent.append("<img src=\"").append(escapeHtml(apod.getUrl())).append("\" alt=\"").append(escapeHtml(apod.getTitle())).append("\" class=\"apod-image\">");
                    htmlContent.append("<div class=\"apod-explanation\">").append(escapeHtml(apod.getExplanation())).append("</div>");
                    htmlContent.append("<div class=\"apod-meta\">");
                    htmlContent.append("<strong>Date:</strong> ").append(escapeHtml(apod.getDate()));
                    if (apod.getCopyright() != null && !apod.getCopyright().isEmpty()) {
                        htmlContent.append(" | <strong>Copyright:</strong> ").append(escapeHtml(apod.getCopyright()));
                    }
                    htmlContent.append("</div>");
                    htmlContent.append("</div>");
                } catch (Exception e) {
                    log.error("Error adding APOD to HTML: {}", e.getMessage(), e);
                }
            }

            htmlContent.append("</div>");
            htmlContent.append("<div class=\"footer\">");
            htmlContent.append("<p>This alert was generated automatically by NASA's Near-Earth Object monitoring system.</p>");
            htmlContent.append("<p>Generated on: ").append(LocalDateTime.now().toString()).append("</p>");
            htmlContent.append("</div>");
            htmlContent.append("</div>");
            htmlContent.append("</body>");
            htmlContent.append("</html>");

            return htmlContent.toString();
            
        } catch (Exception e) {
            log.error("Critical error creating HTML email content: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getRiskLevel(BigDecimal missDistance) {
        try {
            double distance = missDistance.doubleValue();
            if (distance < 1000000) { // Less than 1 million km
                return "<span class=\"danger\">🔴 HIGH</span>";
            } else if (distance < 5000000) { // Less than 5 million km
                return "<span class=\"warning\">🟡 MEDIUM</span>";
            } else {
                return "<span class=\"info\">🟢 LOW</span>";
            }
        } catch (Exception e) {
            log.error("Error calculating risk level for distance {}: {}", missDistance, e.getMessage());
            return "<span class=\"info\">🟢 LOW</span>"; // Default to low risk
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
