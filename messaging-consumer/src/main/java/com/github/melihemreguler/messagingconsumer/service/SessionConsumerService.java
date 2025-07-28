package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.dto.SessionDto;
import com.github.melihemreguler.messagingconsumer.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SessionConsumerService {
    
    private static final Logger log = LoggerFactory.getLogger(SessionConsumerService.class);
    
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    
    public SessionConsumerService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        log.info("SessionConsumerService initialized with repository: {}", sessionRepository.getClass().getSimpleName());
    }
    
    @KafkaListener(topics = "${app.kafka.topics.session-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSessionEvent(String message) {
        
        try {
            log.info("Received session event message: {}", message);
            
            SessionEvent sessionEvent = objectMapper.readValue(message, SessionEvent.class);
            
            log.info("Parsed session event: {} for user: {}", 
                     sessionEvent.getCommand(), sessionEvent.getUserId());
            
            switch (sessionEvent.getCommand()) {
                case "SAVE_SESSION" -> handleSessionCreated(sessionEvent);
                case "UPSERT_SESSION" -> handleSessionUpsert(sessionEvent);
                case "UPDATE_SESSION" -> handleSessionUpdated(sessionEvent);
                case "DELETE_SESSION" -> handleSessionDeleted(sessionEvent);
                case "EXPIRE_SESSION" -> handleSessionExpired(sessionEvent);
                default -> log.warn("Unknown session command: {}", sessionEvent.getCommand());
            }
            
            log.debug("Successfully processed session event: {}", sessionEvent.getCommand());
            
        } catch (Exception e) {
            log.error("Error processing session event: {}", message, e);
            throw new RuntimeException("Failed to process session event", e);
        }
    }
    
    private void handleSessionCreated(SessionEvent event) {
        log.info("Creating session for user: {}", event.getUserId());
        
        SessionDto session = new SessionDto(
            event.getHashedSessionId(),
            event.getUserId(),
            event.getExpiresAt(),
            event.getIpAddress(),
            event.getUserAgent()
        );
        
        session.setCreatedAt(event.getTimestamp());
        session.setLastAccessedAt(event.getTimestamp());
        
        sessionRepository.save(session);
        log.info("Session created successfully for user: {}", event.getUserId());
    }
    
    private void handleSessionUpsert(SessionEvent event) {
        log.info("Upserting session for user: {}", event.getUserId());
        
        // First, try to find existing session by userId
        var existingSessions = sessionRepository.findByUserId(event.getUserId());
        
        if (!existingSessions.isEmpty()) {
            // Update existing session
            SessionDto existingSession = existingSessions.get(0); // Take the first one
            existingSession.setHashedSessionId(event.getHashedSessionId());
            existingSession.setExpiresAt(event.getExpiresAt());
            existingSession.setIpAddress(event.getIpAddress());
            existingSession.setUserAgent(event.getUserAgent());
            existingSession.updateLastAccessed();
            
            sessionRepository.save(existingSession);
            
            // Delete any additional sessions for this user (keep only one)
            if (existingSessions.size() > 1) {
                for (int i = 1; i < existingSessions.size(); i++) {
                    sessionRepository.delete(existingSessions.get(i));
                }
                log.info("Removed {} duplicate sessions for user: {}", existingSessions.size() - 1, event.getUserId());
            }
            
            log.info("Session updated successfully for user: {}", event.getUserId());
        } else {
            // Create new session
            SessionDto session = new SessionDto(
                event.getHashedSessionId(),
                event.getUserId(),
                event.getExpiresAt(),
                event.getIpAddress(),
                event.getUserAgent()
            );
            
            session.setCreatedAt(event.getTimestamp());
            session.setLastAccessedAt(event.getTimestamp());
            
            sessionRepository.save(session);
            log.info("New session created successfully for user: {}", event.getUserId());
        }
    }
    
    private void handleSessionUpdated(SessionEvent event) {
        log.info("Updating last access time for session of user: {}", event.getUserId());
        
        // Find session by userId (since we maintain one session per user)
        var existingSessions = sessionRepository.findByUserId(event.getUserId());
        
        if (!existingSessions.isEmpty()) {
            SessionDto session = existingSessions.get(0);
            session.updateLastAccessed();
            // Optionally update IP and User-Agent if provided
            if (event.getIpAddress() != null) {
                session.setIpAddress(event.getIpAddress());
            }
            if (event.getUserAgent() != null) {
                session.setUserAgent(event.getUserAgent());
            }
            sessionRepository.save(session);
            log.info("Session last access updated for user: {}", event.getUserId());
        } else {
            log.warn("No session found to update for user: {}", event.getUserId());
        }
    }
    
    private void handleSessionDeleted(SessionEvent event) {
        log.info("Deleting session for user: {}", event.getUserId());
        
        sessionRepository.findByHashedSessionId(event.getHashedSessionId())
            .ifPresentOrElse(
                session -> {
                    sessionRepository.delete(session);
                    log.info("Session deleted successfully for user: {}", event.getUserId());
                },
                () -> log.warn("Session not found for deletion: {}", event.getHashedSessionId())
            );
    }
    
    private void handleSessionExpired(SessionEvent event) {
        log.info("Handling expired session for user: {}", event.getUserId());
        
        sessionRepository.findByHashedSessionId(event.getHashedSessionId())
            .ifPresentOrElse(
                session -> {
                    sessionRepository.delete(session);
                    log.info("Expired session deleted for user: {}", event.getUserId());
                },
                () -> log.warn("Session not found for expiration: {}", event.getHashedSessionId())
            );
    }
}
