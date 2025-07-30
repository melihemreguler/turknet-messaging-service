package com.github.melihemreguler.messagingconsumer.strategy.session.impl;

import com.github.melihemreguler.messagingconsumer.dto.SessionDto;
import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateSessionStrategyTest {
    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private UpdateSessionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldUpdateLastAccessedIfSessionExists() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setUserId("user1");
        event.setIpAddress("127.0.0.1");
        event.setUserAgent("JUnit");
        SessionDto session = spy(new SessionDto("session123", "user1", LocalDateTime.now().plusHours(1), "oldIp", "oldAgent"));
        List<SessionDto> sessions = List.of(session);
        when(sessionRepository.findByUserId("user1")).thenReturn(sessions);
        when(sessionRepository.save(any(SessionDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        verify(session).updateLastAccessed();
        verify(session).setIpAddress("127.0.0.1");
        verify(session).setUserAgent("JUnit");
        verify(sessionRepository).save(session);
    }

    @Test
    void execute_shouldWarnIfNoSessionFound() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setUserId("user2");
        when(sessionRepository.findByUserId("user2")).thenReturn(Collections.emptyList());

        //WHEN
        strategy.execute(event);

        //THEN
        verify(sessionRepository, never()).save(any());
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
