// File: src/main/java/com/example/freshfarmjuba/repository/UserRepository.java
package com.example.freshfarmjuba.repository;

import com.example.freshfarmjuba.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (returns User or null)
    User findByEmail(String email);

    // Find by email returning Optional
    Optional<User> findOptionalByEmail(String email);

    // Find by verification token
    User findByVerificationToken(String token);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users with unverified email
    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime dateTime);

    // FIX 1: For fixed number of recent users (top 5)
    List<User> findTop5ByOrderByCreatedAtDesc();

    // FIX 2: For variable limit using @Query
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC LIMIT :limit")
    List<User> findRecentUsers(@Param("limit") int limit);

    // Update failed attempts
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempts = :failedAttempts WHERE u.email = :email")
    void updateFailedAttempts(@Param("failedAttempts") int failedAttempts, @Param("email") String email);

    // Update account lock status
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.accountNonLocked = :locked, u.lockTime = :lockTime WHERE u.email = :email")
    void updateAccountLockStatus(@Param("email") String email, @Param("locked") boolean locked, @Param("lockTime") LocalDateTime lockTime);
}