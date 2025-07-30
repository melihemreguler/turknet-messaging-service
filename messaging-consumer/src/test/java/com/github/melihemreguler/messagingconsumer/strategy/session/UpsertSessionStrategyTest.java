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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpsertSessionStrategyTest {
    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private UpsertSessionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldInsertNewSessionIfNoneExists() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setHashedSessionId("session123");
        event.setUserId("user1");
        event.setExpiresAt(LocalDateTime.now().plusHours(1));
        event.setIpAddress("127.0.0.1");
        event.setUserAgent("JUnit");
        event.setTimestamp(LocalDateTime.now());
        when(sessionRepository.findByUserId("user1")).thenReturn(Collections.emptyList());
        when(sessionRepository.save(any(SessionDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        ArgumentCaptor<SessionDto> captor = ArgumentCaptor.forClass(SessionDto.class);
        verify(sessionRepository).save(captor.capture());
        SessionDto saved = captor.getValue();
        assertEquals("session123", saved.getHashedSessionId());
        assertEquals("user1", saved.getUserId());
    }

    @Test
    void execute_shouldUpdateExistingSessionAndDeleteOthers() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setHashedSessionId("session456");
        event.setUserId("user2");
        event.setExpiresAt(LocalDateTime.now().plusHours(2));
        event.setIpAddress("127.0.0.2");
        event.setUserAgent("JUnit");
        event.setTimestamp(LocalDateTime.now());
        SessionDto session1 = new SessionDto("sessionOld", "user2", LocalDateTime.now().plusHours(1), "oldIp", "oldAgent");
        SessionDto session2 = new SessionDto("sessionExtra", "user2", LocalDateTime.now().plusHours(1), "extraIp", "extraAgent");
        List<SessionDto> sessions = List.of(session1, session2);
        when(sessionRepository.findByUserId("user2")).thenReturn(sessions);
        when(sessionRepository.save(any(SessionDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        ArgumentCaptor<SessionDto> captor = ArgumentCaptor.forClass(SessionDto.class);
        verify(sessionRepository).save(captor.capture());
        SessionDto updated = captor.getValue();
        assertEquals("session456", updated.getHashedSessionId());
        assertEquals("user2", updated.getUserId());
        verify(sessionRepository).delete(session2);
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
