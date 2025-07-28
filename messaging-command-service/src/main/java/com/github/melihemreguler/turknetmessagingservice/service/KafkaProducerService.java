package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.config.MessagingConfig;
import com.github.melihemreguler.turknetmessagingservice.exception.MessageSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessagingConfig messagingConfig;
    private final ObjectMapper objectMapper;
    
    // Constructor to configure ObjectMapper
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, 
                               MessagingConfig messagingConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.messagingConfig = messagingConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void sendMessageCommand(Object messageCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageCommand);
            kafkaTemplate.send(messagingConfig.getMessageCommands(), userId, jsonMessage);
            log.debug("Message command sent to topic: {} with userId key: {}", messagingConfig.getMessageCommands(), userId);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("Failed to serialize message command", e);
        }
    }
    
    public void sendUserCommand(Object userCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(userCommand);
            kafkaTemplate.send(messagingConfig.getUserCommands(), userId, jsonMessage);
            log.debug("User command sent to topic: {} with userId key: {}", messagingConfig.getUserCommands(), userId);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("Failed to serialize user command", e);
        }
    }
    
    public void sendSessionCommand(Object sessionCommand, String userId) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(sessionCommand);
            kafkaTemplate.send(messagingConfig.getSessionCommands(), userId, jsonMessage);
            log.debug("Session command sent to topic: {} with userId key: {}", messagingConfig.getSessionCommands(), userId);
        } catch (JsonProcessingException e) {
            throw new MessageSerializationException("Failed to serialize session command", e);
        }
    }
}
