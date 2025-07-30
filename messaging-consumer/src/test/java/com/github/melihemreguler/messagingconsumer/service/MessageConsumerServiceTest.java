package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.melihemreguler.messagingconsumer.dto.MessageDto;
import com.github.melihemreguler.messagingconsumer.model.MessageCommandEvent;
import com.github.melihemreguler.messagingconsumer.repository.MessageRepository;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.enums.MessageStatus;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import com.github.melihemreguler.messagingconsumer.exception.UnknownMessageCommandException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageConsumerServiceTest {
    @Test
    void givenRetryMessageCommand_whenConsumeMessageCommandRetry_thenProcessesOrRetries() throws Exception {
        //GIVEN
        MessageCommandEvent event = new MessageCommandEvent("SEND_MESSAGE", "thread1", "user1", "username", "content", java.time.LocalDateTime.now());
        String message = "{\"command\":\"SEND_MESSAGE\"}";
        when(objectMapper.readValue(message, MessageCommandEvent.class)).thenReturn(event);

        //WHEN
        service.consumeMessageCommandRetry(message, "0");

        //THEN
        verify(messageRepository, atLeastOnce()).save(any());
    }
    private MessageConsumerService service;
    private MessageRepository messageRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaRetryConfig retryConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        retryConfig = mock(KafkaRetryConfig.class);
        objectMapper = mock(ObjectMapper.class);
        when(retryConfig.getMaxRetry()).thenReturn(5);
        service = new MessageConsumerService(messageRepository, kafkaTemplate, retryConfig, objectMapper);
        // Set messageCommandsRetryTopic field via reflection for tests
        try {
            java.lang.reflect.Field topicField = MessageConsumerService.class.getDeclaredField("messageCommandsRetryTopic");
            topicField.setAccessible(true);
            topicField.set(service, "test-retry-topic");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Mock KafkaTemplate.send to return a mock CompletableFuture
        java.util.concurrent.CompletableFuture mockFuture = mock(java.util.concurrent.CompletableFuture.class);
        // Chain thenAccept and exceptionally to always return mockFuture
        when(mockFuture.thenAccept(any())).thenReturn(mockFuture);
        when(mockFuture.exceptionally(any())).thenReturn(mockFuture);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(mockFuture);
    }

    @Test
    void givenSendMessageCommand_whenConsume_thenProcessMessage() throws Exception {
        //GIVEN
        MessageCommandEvent event = new MessageCommandEvent("SEND_MESSAGE", "thread1", "user1", "username", "content", java.time.LocalDateTime.now());
        String message = "{\"command\":\"SEND_MESSAGE\"}";
        when(objectMapper.readValue(message, MessageCommandEvent.class)).thenReturn(event);
        //WHEN
        service.consumeMessageCommand(message, "0");
        //THEN
        verify(messageRepository, atLeastOnce()).save(any());
    }

    @Test
    void givenUnknownCommand_whenConsume_thenThrowsAndRetries() throws Exception {
        //GIVEN
        MessageCommandEvent event = new MessageCommandEvent("UNKNOWN_COMMAND", "thread1", "user1", "username", "content", java.time.LocalDateTime.now());
        String message = "{\"command\":\"UNKNOWN_COMMAND\"}";
        when(objectMapper.readValue(message, MessageCommandEvent.class)).thenReturn(event);
        //WHEN
        service.consumeMessageCommand(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }

    @Test
    void givenException_whenConsume_thenRetries() throws Exception {
        //GIVEN
        String message = "invalid-json";
        when(objectMapper.readValue(message, MessageCommandEvent.class)).thenThrow(new RuntimeException("Parse error"));
        //WHEN
        service.consumeMessageCommand(message, "0");
        //THEN
        verify(kafkaTemplate, atLeastOnce()).send(any(ProducerRecord.class));
    }
}
