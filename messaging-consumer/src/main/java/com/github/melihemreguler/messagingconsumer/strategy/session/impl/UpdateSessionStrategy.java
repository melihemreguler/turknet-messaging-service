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
public class UpdateSessionStrategy implements SessionCommandStrategy {
    
    private final SessionRepository sessionRepository;
    
    @Override
    public void execute(SessionEvent event) {
        log.info("Updating last access time for session of user: {}", event.getUserId());
        
        var existingSessions = sessionRepository.findByUserId(event.getUserId());
        
        if (!existingSessions.isEmpty()) {
            SessionDto session = existingSessions.get(0);
            session.updateLastAccessed();
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
}
