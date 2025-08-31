package com.onion.emailnotificationservice.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailSenderService {

    @Value("${email.service.from.email}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public EmailSendResult sendBulkEmails(List<String> toEmails, String htmlContent) {
        AtomicInteger successfulEmails = new AtomicInteger(0);
        AtomicInteger failedEmails = new AtomicInteger(0);
        String subject = "NASA Asteroid Alert - Close Approach Detected";
        List<CompletableFuture<Boolean>> emailFutures = toEmails.stream()
            .map((String toEmail) -> CompletableFuture.supplyAsync(() -> sendHtmlEmail(toEmail, subject, htmlContent)))
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

        return new EmailSendResult(successfulEmails.get(), failedEmails.get());
    }

    public boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setFrom(fromEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info("HTML email sent successfully to: {} (attempt {})", toEmail, retryCount + 1);
                return true;
            } catch (MessagingException e) {
                retryCount++;
                log.warn("Failed to send HTML email to {} (attempt {}/{}): {}", toEmail, retryCount, maxRetries, e.getMessage());
                if (retryCount >= maxRetries) {
                    log.error("Failed to send HTML email to {} after {} attempts: {}", toEmail, maxRetries, e.getMessage(), e);
                    return false;
                }
                try {
                    Thread.sleep(1000 * retryCount);
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

    public static class EmailSendResult {
        private final int successfulCount;
        private final int failedCount;

        public EmailSendResult(int successfulCount, int failedCount) {
            this.successfulCount = successfulCount;
            this.failedCount = failedCount;
        }

        public int getSuccessfulCount() {
            return successfulCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public boolean hasSuccessfulEmails() {
            return successfulCount > 0;
        }
    }
}
