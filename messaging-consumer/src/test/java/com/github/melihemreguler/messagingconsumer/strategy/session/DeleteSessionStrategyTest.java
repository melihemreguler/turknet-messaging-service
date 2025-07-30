package com.github.melihemreguler.messagingconsumer.strategy.session.impl;

import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteSessionStrategyTest {
    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private DeleteSessionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldDeleteSessionIfExists() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setHashedSessionId("session123");
        event.setUserId("user1");
        var sessionDto = mock(com.github.melihemreguler.messagingconsumer.dto.SessionDto.class);
        when(sessionRepository.findByHashedSessionId("session123")).thenReturn(Optional.of(sessionDto));

        //WHEN
        strategy.execute(event);

        //THEN
        verify(sessionRepository).delete(sessionDto);
    }

    @Test
    void execute_shouldWarnIfSessionNotFound() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setHashedSessionId("session999");
        event.setUserId("user1");
        when(sessionRepository.findByHashedSessionId("session999")).thenReturn(Optional.empty());

        //WHEN
        strategy.execute(event);

        //THEN
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void execute_shouldHandleNullEvent() {
        //GIVEN
        SessionEvent event = null;

        //WHEN
        assertThrows(NullPointerException.class, () -> strategy.execute(event));

        //THEN
        // Exception expected
    }
}
