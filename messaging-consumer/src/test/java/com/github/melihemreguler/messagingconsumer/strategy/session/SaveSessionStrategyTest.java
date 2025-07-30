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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SaveSessionStrategyTest {
    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SaveSessionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldSaveSession() {
        //GIVEN
        SessionEvent event = new SessionEvent();
        event.setHashedSessionId("session123");
        event.setUserId("user1");
        event.setExpiresAt(LocalDateTime.now().plusHours(1));
        event.setIpAddress("127.0.0.1");
        event.setUserAgent("JUnit");
        event.setTimestamp(LocalDateTime.now());
        when(sessionRepository.save(any(SessionDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        ArgumentCaptor<SessionDto> captor = ArgumentCaptor.forClass(SessionDto.class);
        verify(sessionRepository).save(captor.capture());
        SessionDto saved = captor.getValue();
        assertEquals("session123", saved.getHashedSessionId());
        assertEquals("user1", saved.getUserId());
        assertEquals(event.getExpiresAt(), saved.getExpiresAt());
        assertEquals("127.0.0.1", saved.getIpAddress());
        assertEquals("JUnit", saved.getUserAgent());
        assertEquals(event.getTimestamp(), saved.getCreatedAt());
        assertEquals(event.getTimestamp(), saved.getLastAccessedAt());
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
