package com.example.freshfarmjuba.service;

import com.example.freshfarmjuba.model.User;
import com.example.freshfarmjuba.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by email: {}", email);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            logger.error("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        logger.debug("User found: {} with roles: {}", user.getEmail(), user.getRoles());
        
        if (!user.isEnabled()) {
            logger.warn("User account is disabled: {}", email);
        }
        
        if (!user.isAccountNonLocked()) {
            logger.warn("User account is locked: {}", email);
        }

        // Return the user entity directly as it implements UserDetails
        return user;
    }

    @Transactional(readOnly = true)
    public User loadUserEntityByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserByEmailOptional(String email) {
        return userRepository.findOptionalByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void updateFailedAttempts(String email, int attempts) {
        userRepository.updateFailedAttempts(attempts, email);
    }

    @Transactional
    public void lockUserAccount(String email) {
        userRepository.updateAccountLockStatus(email, false, LocalDateTime.now());
    }

    @Transactional
    public void unlockUserAccount(String email) {
        userRepository.updateAccountLockStatus(email, true, null);
    }
}