package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.SessionDto;
import com.github.melihemreguler.turknetmessagingservice.model.SessionEvent;
import com.github.melihemreguler.turknetmessagingservice.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    
    @Value("${app.session.expiration-hours:24}")
    private int sessionExpirationHours;
    
    @Value("${app.session.cleanup-interval-minutes:60}")
    private int sessionCleanupIntervalMinutes;
    
    public String createSession(String userId, String username, String ipAddress, String userAgent) {
        try {
            // Generate new session token
            String sessionToken = UUID.randomUUID().toString();
            String hashedSessionToken = passwordEncoder.encode(sessionToken);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(sessionExpirationHours);
            
            // Send upsert command to writer-service via Kafka
            SessionEvent sessionEvent = SessionEvent.createOrUpdate(
                hashedSessionToken, userId, expiresAt, ipAddress, userAgent);
            
            kafkaProducerService.sendSessionCommand(sessionEvent);
            
            log.info("Session created/updated for user: {} (ID: {}) with expiration: {}", username, userId, expiresAt);
            return sessionToken;
            
        } catch (Exception e) {
            log.error("Error creating session for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to create session", e);
        }
    }
    
    public Optional<SessionDto> validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            // Find session by checking all sessions and matching the hashed token
            return sessionRepository.findAll().stream()
                .filter(session -> !session.isExpired())
                .filter(session -> passwordEncoder.matches(sessionToken, session.getHashedSessionToken()))
                .findFirst();
                
        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public void invalidateSession(String sessionToken) {
        try {
            Optional<SessionDto> sessionOpt = validateSession(sessionToken);
            if (sessionOpt.isPresent()) {
                SessionDto session = sessionOpt.get();
                sessionRepository.delete(session);
                log.info("Session invalidated for user: {}", session.getUserId());
            }
        } catch (Exception e) {
            log.error("Error invalidating session: {}", e.getMessage(), e);
        }
    }
    
    public void cleanupExpiredSessions() {
        try {
            List<SessionDto> expiredSessions = sessionRepository.findAll().stream()
                .filter(SessionDto::isExpired)
                .toList();
                
            if (!expiredSessions.isEmpty()) {
                sessionRepository.deleteAll(expiredSessions);
                log.info("Cleaned up {} expired sessions", expiredSessions.size());
            } else {
                log.debug("No expired sessions found during cleanup");
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions: {}", e.getMessage(), e);
        }
    }
    
    @Scheduled(fixedRateString = "#{${app.session.cleanup-interval-minutes:60} * 60 * 1000}")
    public void scheduledCleanupExpiredSessions() {
        log.debug("Running scheduled session cleanup (interval: {} minutes)", sessionCleanupIntervalMinutes);
        cleanupExpiredSessions();
    }
}
