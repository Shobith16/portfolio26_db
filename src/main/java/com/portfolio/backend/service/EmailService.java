package com.portfolio.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@portfolio.com}")
    private String fromEmail;

    public void sendSimpleEmail(String to, String subject, String body) {
        // Check if mail sender is configured
        if (mailSender == null) {
            logger.info("Email service not configured. Email would have been sent:");
            logger.info("To: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Body: {}", body);
            logger.info("---");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            // Log the exception but don't throw - email sending is non-blocking
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            e.printStackTrace();
            // throw new RuntimeException(e); // Optional: rethrow to see 500 if strict
        }
    }

    public void sendMessageToRecruiter(String recipientEmail, String senderName, String message) {
        String subject = "Message from " + senderName;
        String body = "You have received a message from your portfolio contact:\n\n" +
                message + "\n\n" +
                "---\n" +
                "This is an automated message from Portfolio Admin.";

        sendSimpleEmail(recipientEmail, subject, body);
    }

    public void sendPasswordResetOtp(String email, String otp) {
        String subject = "Password Reset OTP - Portfolio Admin";
        String body = "Your OTP for password reset is: " + otp + "\n\n" +
                "This OTP is valid for 15 minutes.\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "---\n" +
                "Portfolio Admin";

        sendSimpleEmail(email, subject, body);
    }
}
