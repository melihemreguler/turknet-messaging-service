package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.SessionDto;
import com.github.melihemreguler.turknetmessagingservice.model.event.SessionEvent;
import com.github.melihemreguler.turknetmessagingservice.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {
    @Test
    void givenValidSessionIdAndUserId_whenValidateSession_thenReturnsSessionDto() {
        String sessionId = UUID.randomUUID().toString();
        String userId = "user-id";
        SessionDto sessionDto = new SessionDto();
        sessionDto.setHashedSessionId("hashed-session");
        sessionDto.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findByUserId(userId)).thenReturn(List.of(sessionDto));
        when(passwordEncoder.matches(sessionId, "hashed-session")).thenReturn(true);

        Optional<SessionDto> result = sessionService.validateSession(sessionId, userId);

        assertTrue(result.isPresent());
        assertEquals("hashed-session", result.get().getHashedSessionId());
    }

    @Test
    void givenInvalidSessionIdOrUserId_whenValidateSession_thenReturnsEmpty() {
        assertTrue(sessionService.validateSession(null, "user-id").isEmpty());
        assertTrue(sessionService.validateSession("session-id", null).isEmpty());
        assertTrue(sessionService.validateSession("", "user-id").isEmpty());
        assertTrue(sessionService.validateSession("session-id", "").isEmpty());
    }

    @Test
    void givenException_whenValidateSessionWithUserId_thenReturnsEmpty() {
        String sessionId = UUID.randomUUID().toString();
        String userId = "user-id";
        when(sessionRepository.findByUserId(userId)).thenThrow(new RuntimeException("fail"));
        Optional<SessionDto> result = sessionService.validateSession(sessionId, userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenValidSessionId_whenInvalidateSession_thenDeletesSession() {
        String sessionId = UUID.randomUUID().toString();
        SessionDto sessionDto = new SessionDto();
        sessionDto.setHashedSessionId("hashed-session");
        sessionDto.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findAll()).thenReturn(List.of(sessionDto));
        when(passwordEncoder.matches(sessionId, "hashed-session")).thenReturn(true);
        doNothing().when(sessionRepository).delete(sessionDto);

        sessionService.invalidateSession(sessionId);
        verify(sessionRepository).delete(sessionDto);
    }

    @Test
    void givenNoValidSession_whenInvalidateSession_thenNoDeleteCalled() {
        String sessionId = UUID.randomUUID().toString();
        SessionDto sessionDto = new SessionDto();
        sessionDto.setHashedSessionId("hashed-session");
        sessionDto.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findAll()).thenReturn(List.of(sessionDto));
        when(passwordEncoder.matches(sessionId, "hashed-session")).thenReturn(false);

        sessionService.invalidateSession(sessionId);
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void givenExpiredSessions_whenCleanupExpiredSessions_thenDeletesExpiredSessions() {
        SessionDto expired = new SessionDto();
        expired.setExpiresAt(LocalDateTime.now().minusHours(1));
        when(sessionRepository.findAll()).thenReturn(List.of(expired));
        doNothing().when(sessionRepository).deleteAll(List.of(expired));

        sessionService.cleanupExpiredSessions();
        verify(sessionRepository).deleteAll(List.of(expired));
    }

    @Test
    void givenException_whenCleanupExpiredSessions_thenNoDeleteCalled() {
        when(sessionRepository.findAll()).thenThrow(new RuntimeException("fail"));
        sessionService.cleanupExpiredSessions();
        verify(sessionRepository, never()).deleteAll(any());
    }
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidUser_whenCreateSession_thenReturnsSessionToken() {
        // Given
        String userId = "user-id";
        String username = "user";
        String ipAddress = "127.0.0.1";
        String userAgent = "agent";
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-session");
        doNothing().when(kafkaProducerService).sendSessionCommand(any(SessionEvent.class), eq(userId));

        // When
        String token = sessionService.createSession(userId, username, ipAddress, userAgent);

        // Then
        assertNotNull(token);
        verify(kafkaProducerService).sendSessionCommand(any(SessionEvent.class), eq(userId));
    }

    @Test
    void givenValidSessionId_whenValidateSession_thenReturnsSessionDto() {
        // Given
        String sessionId = UUID.randomUUID().toString();
        SessionDto sessionDto = new SessionDto();
        sessionDto.setHashedSessionId("hashed-session");
        sessionDto.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findAll()).thenReturn(List.of(sessionDto));
        when(passwordEncoder.matches(sessionId, "hashed-session")).thenReturn(true);

        // When
        Optional<SessionDto> result = sessionService.validateSession(sessionId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("hashed-session", result.get().getHashedSessionId());
    }

    @Test
    void givenInvalidSessionId_whenValidateSession_thenReturnsEmpty() {
        // Given
        String sessionId = UUID.randomUUID().toString();
        SessionDto sessionDto = new SessionDto();
        sessionDto.setHashedSessionId("hashed-session");
        sessionDto.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(sessionRepository.findAll()).thenReturn(List.of(sessionDto));
        when(passwordEncoder.matches(sessionId, "hashed-session")).thenReturn(false);

        // When
        Optional<SessionDto> result = sessionService.validateSession(sessionId);

        // Then
        assertTrue(result.isEmpty());
    }
}
