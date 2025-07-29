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
public class SaveSessionStrategy implements SessionCommandStrategy {
    
    private final SessionRepository sessionRepository;
    
    @Override
    public void execute(SessionEvent event) {
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
}
