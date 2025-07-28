package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.SessionDto;
import com.github.melihemreguler.turknetmessagingservice.model.event.SessionEvent;
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
        // Generate new session token
        String sessionToken = UUID.randomUUID().toString();
        String hashedSessionId = passwordEncoder.encode(sessionToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(sessionExpirationHours);
        
        // Send upsert command to writer-service via Kafka
        SessionEvent sessionEvent = SessionEvent.createOrUpdate(
            hashedSessionId, userId, expiresAt, ipAddress, userAgent);
        
        kafkaProducerService.sendSessionCommand(sessionEvent, userId);
        
        log.info("Session created/updated for user: {} (ID: {}) with expiration: {}", username, userId, expiresAt);
        return sessionToken;
    }
    
    public Optional<SessionDto> validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            // Find session by checking all sessions and matching the hashed token
            return sessionRepository.findAll().stream()
                .filter(session -> !session.isExpired())
                .filter(session -> passwordEncoder.matches(sessionId, session.getHashedSessionId()))
                .findFirst();
                
        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<SessionDto> validateSession(String sessionId, String userId) {
        if (sessionId == null || sessionId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            // Find sessions only for the specific user and match the hashed token
            return sessionRepository.findByUserId(userId).stream()
                .filter(session -> !session.isExpired())
                .filter(session -> passwordEncoder.matches(sessionId, session.getHashedSessionId()))
                .findFirst();
                
        } catch (Exception e) {
            log.error("Error validating session for user {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public void invalidateSession(String sessionId) {
        Optional<SessionDto> sessionOpt = validateSession(sessionId);
        if (sessionOpt.isPresent()) {
            SessionDto session = sessionOpt.get();
            sessionRepository.delete(session);
            log.info("Session invalidated for user: {}", session.getUserId());
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
