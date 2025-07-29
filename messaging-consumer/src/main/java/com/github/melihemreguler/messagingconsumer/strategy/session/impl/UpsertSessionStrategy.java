package com.github.melihemreguler.messagingconsumer.strategy.session.impl;

import com.github.melihemreguler.messagingconsumer.dto.SessionDto;
import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.repository.SessionRepository;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpsertSessionStrategy implements SessionCommandStrategy {
    
    private final SessionRepository sessionRepository;
    
    @Override
    public void execute(SessionEvent event) {
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
}
