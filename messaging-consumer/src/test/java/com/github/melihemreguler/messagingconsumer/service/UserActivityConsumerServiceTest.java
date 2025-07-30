package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.enums.UserActivityCommand;
import com.github.melihemreguler.messagingconsumer.model.UserActivityEvent;
import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategy;
import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategyFactory;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserActivityConsumerServiceTest {
    private UserActivityConsumerService service;
    private ObjectMapper objectMapper;
    private UserActivityStrategyFactory strategyFactory;
    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaRetryConfig retryConfig;
    private UserActivityStrategy strategy;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        strategyFactory = mock(UserActivityStrategyFactory.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        retryConfig = mock(KafkaRetryConfig.class);
        strategy = mock(UserActivityStrategy.class);
        when(retryConfig.getMaxRetry()).thenReturn(3);
        service = new UserActivityConsumerService(objectMapper, strategyFactory, kafkaTemplate, retryConfig);
        // Set userCommandsRetryTopic field via reflection
        try {
            java.lang.reflect.Field topicField = UserActivityConsumerService.class.getDeclaredField("userCommandsRetryTopic");
            topicField.setAccessible(true);
            topicField.set(service, "test-user-retry-topic");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        java.util.concurrent.CompletableFuture mockFuture = mock(java.util.concurrent.CompletableFuture.class);
        when(mockFuture.thenAccept(any())).thenReturn(mockFuture);
        when(mockFuture.exceptionally(any())).thenReturn(mockFuture);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(mockFuture);
    }

    @Test
    void givenValidUserActivityEvent_whenConsumeUserActivity_thenStrategyExecutes() throws Exception {
        //GIVEN
        UserActivityEvent event = new UserActivityEvent(
            "LOGIN_ATTEMPT", // command
            "username1", // username
            "user1", // userId
            "127.0.0.1", // ipAddress
            "Mozilla", // userAgent
            true, // successful
            java.time.LocalDateTime.now(), // timestamp
            null, // failureReason
            "user1@example.com" // email
        );
        String message = "{\"command\":\"LOGIN_ATTEMPT\"}";
        when(objectMapper.readValue(message, UserActivityEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(eq(UserActivityCommand.fromString(event.getCommand())))).thenReturn(strategy);
        //WHEN
        service.consumeUserActivity(message, "0");
        //THEN
        verify(strategy, times(1)).execute(event);
    }

    @Test
    void givenUnknownCommand_whenConsumeUserActivity_thenRetries() throws Exception {
        //GIVEN
        UserActivityEvent event = new UserActivityEvent(
            "UNKNOWN", "username2", "user1",
            "127.0.0.1", "Mozilla", false,
            java.time.LocalDateTime.now(), "Some failure", "user2@example.com"
        );
        String message = "{\"command\":\"UNKNOWN\"}";
        when(objectMapper.readValue(message, UserActivityEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(any())).thenReturn(null);
        //WHEN
        service.consumeUserActivity(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test
    void givenJsonException_whenConsumeUserActivity_thenRetries() throws Exception {
        //GIVEN
        String message = "invalid-json";
        when(objectMapper.readValue(message, UserActivityEvent.class)).thenThrow(new RuntimeException("Parse error"));
        //WHEN
        service.consumeUserActivity(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test
    void givenValidUserActivityEvent_whenConsumeUserActivityRetry_thenStrategyExecutes() throws Exception {
        //GIVEN
        UserActivityEvent event = new UserActivityEvent(
            "LOGIN_ATTEMPT", "username3", "user1",
            "127.0.0.1", "Mozilla", true,
            java.time.LocalDateTime.now(), null, "user3@example.com"
        );
        String message = "{\"command\":\"LOGIN_ATTEMPT\"}";
        when(objectMapper.readValue(message, UserActivityEvent.class)).thenReturn(event);
        when(strategyFactory.getStrategy(eq(UserActivityCommand.fromString(event.getCommand())))).thenReturn(strategy);
        //WHEN
        service.consumeUserActivityRetry(message, "1");
        //THEN
        verify(strategy, times(1)).execute(event);
    }

    @Test
    void givenMaxRetryExceeded_whenConsumeUserActivity_thenThrowsException() {
        //GIVEN
        String message = "msg";
        int retryCount = 3;
        Exception error = new RuntimeException("fail");
        when(retryConfig.getMaxRetry()).thenReturn(3);
        //WHEN & THEN
        assertThrows(MaxRetryExceededException.class, () ->
            service.consumeUserActivity(message, String.valueOf(retryCount))
        );
    }
}
