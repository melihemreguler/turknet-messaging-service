package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.enums.SessionCommand;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategy;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategyFactory;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import com.github.melihemreguler.messagingconsumer.exception.UnknownSessionCommandException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionConsumerServiceTest {
    private SessionConsumerService service;
    private ObjectMapper objectMapper;
    private SessionCommandStrategyFactory strategyFactory;
    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaRetryConfig retryConfig;
    private SessionCommandStrategy strategy;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        strategyFactory = mock(SessionCommandStrategyFactory.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        retryConfig = mock(KafkaRetryConfig.class);
        strategy = mock(SessionCommandStrategy.class);
        when(retryConfig.getMaxRetry()).thenReturn(3);
        service = new SessionConsumerService(objectMapper, strategyFactory, kafkaTemplate, retryConfig);
        // Set sessionCommandsRetryTopic field via reflection
        try {
            java.lang.reflect.Field topicField = SessionConsumerService.class.getDeclaredField("sessionCommandsRetryTopic");
            topicField.setAccessible(true);
            topicField.set(service, "test-session-retry-topic");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        java.util.concurrent.CompletableFuture mockFuture = mock(java.util.concurrent.CompletableFuture.class);
        when(mockFuture.thenAccept(any())).thenReturn(mockFuture);
        when(mockFuture.exceptionally(any())).thenReturn(mockFuture);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(mockFuture);
    }

    @Test
    void givenValidSessionEvent_whenHandleSessionEvent_thenStrategyExecutes() throws Exception {
        //GIVEN
        // Use all required parameters for SessionEvent
        SessionEvent event = new SessionEvent(
            "SAVE_SESSION", // command
            "hashedSessionId1", // hashedSessionId
            "user1", // userId
            java.time.LocalDateTime.now().plusHours(1), // expiresAt
            "127.0.0.1", // ipAddress
            "Mozilla", // userAgent
            java.time.LocalDateTime.now() // timestamp
        );
        String message = "{\"command\":\"SAVE_SESSION\"}";
        when(objectMapper.readValue(message, SessionEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(eq(SessionCommand.fromString(event.getCommand())))).thenReturn(strategy);
        //WHEN
        service.handleSessionEvent(message, "0");
        //THEN
        verify(strategy, times(1)).execute(event);
    }

    @Test
    void givenUnknownCommand_whenHandleSessionEvent_thenRetries() throws Exception {
        //GIVEN
        SessionEvent event = new SessionEvent(
            "UNKNOWN", "hashedSessionId2", "user1",
            java.time.LocalDateTime.now().plusHours(1), "127.0.0.1", "Mozilla", java.time.LocalDateTime.now()
        );
        String message = "{\"command\":\"UNKNOWN\"}";
        when(objectMapper.readValue(message, SessionEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(any())).thenReturn(null);
        //WHEN
        service.handleSessionEvent(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test
    void givenJsonException_whenHandleSessionEvent_thenRetries() throws Exception {
        //GIVEN
        String message = "invalid-json";
        when(objectMapper.readValue(message, SessionEvent.class)).thenThrow(new RuntimeException("Parse error"));
        //WHEN
        service.handleSessionEvent(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test
    void givenValidSessionEvent_whenHandleSessionEventRetry_thenStrategyExecutes() throws Exception {
        //GIVEN
        SessionEvent event = new SessionEvent(
            "SAVE_SESSION", "hashedSessionId3", "user1",
            java.time.LocalDateTime.now().plusHours(1), "127.0.0.1", "Mozilla", java.time.LocalDateTime.now()
        );
        String message = "{\"command\":\"SAVE_SESSION\"}";
        when(objectMapper.readValue(message, SessionEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(eq(SessionCommand.fromString(event.getCommand())))).thenReturn(strategy);
        //WHEN
        service.handleSessionEventRetry(message, "1");
        //THEN
        verify(strategy, times(1)).execute(event);
    }

    @Test
    void givenMaxRetryExceeded_whenHandleSessionEvent_thenThrowsException() {
        //GIVEN
        String message = "msg";
        int retryCount = 3;
        Exception error = new RuntimeException("fail");
        when(retryConfig.getMaxRetry()).thenReturn(3);
        //WHEN & THEN
        assertThrows(MaxRetryExceededException.class, () ->
            service.handleSessionEvent(message, String.valueOf(retryCount))
        );
    }
}
