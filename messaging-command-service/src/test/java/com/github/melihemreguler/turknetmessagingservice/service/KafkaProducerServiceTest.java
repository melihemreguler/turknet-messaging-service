package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.config.MessagingConfig;
import com.github.melihemreguler.turknetmessagingservice.exception.KafkaPublishingException;
import com.github.melihemreguler.turknetmessagingservice.exception.MessageSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.melihemreguler.turknetmessagingservice.model.event.MessageCommand;
import com.github.melihemreguler.turknetmessagingservice.model.event.SessionEvent;
import com.github.melihemreguler.turknetmessagingservice.model.event.UserActivityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {
    @Test
    void givenValidUserActivityEvent_whenSendUserCommand_thenKafkaSendCalled() throws Exception {
        // Given
        UserActivityEvent event = mock(UserActivityEvent.class);
        when(objectMapper.writeValueAsString(event)).thenReturn("json");
        when(messagingConfig.getUserCommands()).thenReturn("user-topic");

        // When
        kafkaProducerService.sendUserCommand(event, "user-id");

        // Then
        verify(kafkaTemplate).send("user-topic", "user-id", "json");
    }

    @Test
    void givenJsonProcessingException_whenSendUserCommand_thenThrowsMessageSerializationException() throws Exception {
        // Given
        UserActivityEvent event = mock(UserActivityEvent.class);
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("fail"){});
        when(messagingConfig.getUserCommands()).thenReturn("user-topic");

        // When & Then
        assertThrows(MessageSerializationException.class, () ->
                kafkaProducerService.sendUserCommand(event, "user-id"));
    }

    @Test
    void givenKafkaException_whenSendUserCommand_thenThrowsKafkaPublishingException() throws Exception {
        // Given
        UserActivityEvent event = mock(UserActivityEvent.class);
        when(objectMapper.writeValueAsString(event)).thenReturn("json");
        when(messagingConfig.getUserCommands()).thenReturn("user-topic");
        doThrow(new RuntimeException("fail")).when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // When & Then
        assertThrows(KafkaPublishingException.class, () ->
                kafkaProducerService.sendUserCommand(event, "user-id"));
    }

    @Test
    void givenValidSessionEvent_whenSendSessionCommand_thenKafkaSendCalled() throws Exception {
        // Given
        SessionEvent event = mock(SessionEvent.class);
        when(objectMapper.writeValueAsString(event)).thenReturn("json");
        when(messagingConfig.getSessionCommands()).thenReturn("session-topic");

        // When
        kafkaProducerService.sendSessionCommand(event, "user-id");

        // Then
        verify(kafkaTemplate).send("session-topic", "user-id", "json");
    }

    @Test
    void givenJsonProcessingException_whenSendSessionCommand_thenThrowsMessageSerializationException() throws Exception {
        // Given
        SessionEvent event = mock(SessionEvent.class);
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("fail"){});
        when(messagingConfig.getSessionCommands()).thenReturn("session-topic");

        // When & Then
        assertThrows(MessageSerializationException.class, () ->
                kafkaProducerService.sendSessionCommand(event, "user-id"));
    }

    @Test
    void givenKafkaException_whenSendSessionCommand_thenThrowsKafkaPublishingException() throws Exception {
        // Given
        SessionEvent event = mock(SessionEvent.class);
        when(objectMapper.writeValueAsString(event)).thenReturn("json");
        when(messagingConfig.getSessionCommands()).thenReturn("session-topic");
        doThrow(new RuntimeException("fail")).when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // When & Then
        assertThrows(KafkaPublishingException.class, () ->
                kafkaProducerService.sendSessionCommand(event, "user-id"));
    }
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private MessagingConfig messagingConfig;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidMessageCommand_whenSendMessageCommand_thenKafkaSendCalled() throws Exception {
        // Given
        MessageCommand command = mock(MessageCommand.class);
        when(objectMapper.writeValueAsString(command)).thenReturn("json");
        when(messagingConfig.getMessageCommands()).thenReturn("topic");

        // When
        kafkaProducerService.sendMessageCommand(command, "user-id");

        // Then
        verify(kafkaTemplate).send("topic", "user-id", "json");
    }

    @Test
    void givenJsonProcessingException_whenSendMessageCommand_thenThrowsMessageSerializationException() throws Exception {
        // Given
        MessageCommand command = mock(MessageCommand.class);
        when(objectMapper.writeValueAsString(command)).thenThrow(new JsonProcessingException("fail"){});
        when(messagingConfig.getMessageCommands()).thenReturn("topic");

        // When & Then
        assertThrows(MessageSerializationException.class, () ->
                kafkaProducerService.sendMessageCommand(command, "user-id"));
    }

    @Test
    void givenKafkaException_whenSendMessageCommand_thenThrowsKafkaPublishingException() throws Exception {
        // Given
        MessageCommand command = mock(MessageCommand.class);
        when(objectMapper.writeValueAsString(command)).thenReturn("json");
        when(messagingConfig.getMessageCommands()).thenReturn("topic");
        doThrow(new RuntimeException("fail")).when(kafkaTemplate).send(anyString(), anyString(), anyString());

        // When & Then
        assertThrows(KafkaPublishingException.class, () ->
                kafkaProducerService.sendMessageCommand(command, "user-id"));
    }
}
