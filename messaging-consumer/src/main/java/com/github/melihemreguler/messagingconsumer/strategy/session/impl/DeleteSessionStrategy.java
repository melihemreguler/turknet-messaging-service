package com.github.melihemreguler.messagingconsumer.strategy.session.impl;

import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.repository.SessionRepository;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteSessionStrategy implements SessionCommandStrategy {
    
    private final SessionRepository sessionRepository;
    
    @Override
    public void execute(SessionEvent event) {
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
}
