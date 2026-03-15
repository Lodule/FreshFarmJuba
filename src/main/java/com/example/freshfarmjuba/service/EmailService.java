package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send verification email to newly registered user
     */
    public void sendVerificationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Fresh Farm Juba - Verify Your Email Address");

        String verificationLink = baseUrl + "/verify-email?token=" + user.getVerificationToken();

        String emailContent = String.format(
                "Dear %s %s,\n\n" +
                        "Thank you for registering with Fresh Farm Juba! Please verify your email address by clicking the link below:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you did not create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                user.getFirstName(), user.getLastName(), verificationLink
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
            System.out.println("Verification email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            // Log error but don't throw exception - we don't want to fail registration if email fails
        }
    }

    /**
     * Send welcome email after email verification
     */
    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Welcome to Fresh Farm Juba!");

        String emailContent = String.format(
                "Dear %s %s,\n\n" +
                        "Welcome to Fresh Farm Juba! Your email has been successfully verified.\n\n" +
                        "You can now:\n" +
                        "- Browse our fresh farm products\n" +
                        "- Place orders online\n" +
                        "- Track your order history\n" +
                        "- Receive exclusive offers\n\n" +
                        "Visit our website to start shopping: %s\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                user.getFirstName(), user.getLastName(), baseUrl
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Fresh Farm Juba - Password Reset Request");

        String resetLink = baseUrl + "/reset-password?token=" + resetToken;

        String emailContent = String.format(
                "Dear %s %s,\n\n" +
                        "We received a request to reset your password. Click the link below to reset it:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you didn't request a password reset, please ignore this email or contact support.\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                user.getFirstName(), user.getLastName(), resetLink
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(User user, String orderNumber, double orderTotal) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Fresh Farm Juba - Order Confirmation #" + orderNumber);

        String emailContent = String.format(
                "Dear %s %s,\n\n" +
                        "Thank you for your order! Your order has been confirmed.\n\n" +
                        "Order Number: %s\n" +
                        "Order Total: $%.2f\n\n" +
                        "You can track your order status by logging into your account.\n\n" +
                        "We'll notify you when your order ships.\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                user.getFirstName(), user.getLastName(), orderNumber, orderTotal
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdateEmail(User user, String orderNumber, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Fresh Farm Juba - Order #" + orderNumber + " Status Update");

        String emailContent = String.format(
                "Dear %s %s,\n\n" +
                        "Your order #%s has been updated to: %s\n\n" +
                        "Log in to your account to view more details.\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                user.getFirstName(), user.getLastName(), orderNumber, status
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send order status email: " + e.getMessage());
        }
    }

    /**
     * Send contact form auto-reply
     */
    public void sendContactAutoReply(String name, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Fresh Farm Juba - Thank You for Contacting Us");

        String emailContent = String.format(
                "Dear %s,\n\n" +
                        "Thank you for contacting Fresh Farm Juba! We have received your message and will get back to you within 24 hours.\n\n" +
                        "If you have an urgent inquiry, please call us at +211 981 502 973.\n\n" +
                        "Best regards,\n" +
                        "The Fresh Farm Juba Team",
                name
        );

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send contact auto-reply: " + e.getMessage());
        }
    }
}