package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User save(User user) {
        // Check if user already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Generate verification token
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));

        // Set default values
        user.setEmailVerified(false);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(user);

        // Send verification email (async in production)
        try {
            emailService.sendVerificationEmail(savedUser);
        } catch (Exception e) {
            // Log error but don't rollback transaction
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token);
        if (user != null && user.getTokenExpiry() != null &&
                user.getTokenExpiry().isAfter(LocalDateTime.now())) {

            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setTokenExpiry(null);
            userRepository.save(user);

            // Send welcome email
            try {
                emailService.sendWelcomeEmail(user);
            } catch (Exception e) {
                System.err.println("Failed to send welcome email: " + e.getMessage());
            }

            return true;
        }
        return false;
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            if (!user.isEmailVerified()) {
                throw new RuntimeException("Please verify your email before logging in");
            }
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            return user;
        }
        return null;
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public boolean isEmailVerified(String email) {
        User user = userRepository.findByEmail(email);
        return user != null && user.isEmailVerified();
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null && !user.isEmailVerified()) {
            // Generate new token
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            user.setTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            // Send new verification email
            emailService.sendVerificationEmail(user);
        }
    }

    // Add these methods to UserService.java

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    // File: src/main/java/com/example/freshfarmjuba/service/UserService.java
// Update the getRecentUsers method

    public List<User> getRecentUsers(int limit) {
        // Use the custom query method that accepts a limit parameter
        return userRepository.findRecentUsers(limit);
    }

    // Or if you only need top 5, use this method
    public List<User> getTop5RecentUsers() {
        return userRepository.findTop5ByOrderByCreatedAtDesc();
    }
    // File: src/main/java/com/example/freshfarmjuba/service/UserService.java
// Add these missing methods:

// For userService.findAll() - already exists
// For userService.findById(id) - already exists
// For userService.toggleUserStatus(userId) - already exists


}