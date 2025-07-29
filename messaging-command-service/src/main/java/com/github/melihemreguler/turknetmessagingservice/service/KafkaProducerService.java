package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.config.MessagingConfig;
import com.github.melihemreguler.turknetmessagingservice.exception.KafkaPublishingException;
import com.github.melihemreguler.turknetmessagingservice.exception.MessageSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.melihemreguler.turknetmessagingservice.model.event.MessageCommand;
import com.github.melihemreguler.turknetmessagingservice.model.event.SessionEvent;
import com.github.melihemreguler.turknetmessagingservice.model.event.UserActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessagingConfig messagingConfig;
    private final ObjectMapper objectMapper;
    
    public void sendMessageCommand(MessageCommand messageCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageCommand);
            kafkaTemplate.send(messagingConfig.getMessageCommands(), userId, jsonMessage);
            log.debug("Message command sent to topic: {} with userId key: {}", messagingConfig.getMessageCommands(), userId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message command for user {}: {}", userId, e.getMessage(), e);
            throw new MessageSerializationException("Failed to serialize message command", e);
        } catch (Exception e) {
            log.error("Failed to send message command to Kafka for user {}: {}", userId, e.getMessage(), e);
            throw new KafkaPublishingException("Failed to send message command to Kafka", e);
        }
    }
    
    public void sendUserCommand(UserActivityEvent userCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(userCommand);
            kafkaTemplate.send(messagingConfig.getUserCommands(), userId, jsonMessage);
            log.debug("User command sent to topic: {} with userId key: {}", messagingConfig.getUserCommands(), userId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user command for user {}: {}", userId, e.getMessage(), e);
            throw new MessageSerializationException("Failed to serialize user command", e);
        } catch (Exception e) {
            log.error("Failed to send user command to Kafka for user {}: {}", userId, e.getMessage(), e);
            throw new KafkaPublishingException("Failed to send user command to Kafka", e);
        }
    }
    
    public void sendSessionCommand(SessionEvent sessionCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(sessionCommand);
            kafkaTemplate.send(messagingConfig.getSessionCommands(), userId, jsonMessage);
            log.debug("Session command sent to topic: {} with userId key: {}", messagingConfig.getSessionCommands(), userId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session command for user {}: {}", userId, e.getMessage(), e);
            throw new MessageSerializationException("Failed to serialize session command", e);
        } catch (Exception e) {
            log.error("Failed to send session command to Kafka for user {}: {}", userId, e.getMessage(), e);
            throw new KafkaPublishingException("Failed to send session command to Kafka", e);
        }
    }
}
