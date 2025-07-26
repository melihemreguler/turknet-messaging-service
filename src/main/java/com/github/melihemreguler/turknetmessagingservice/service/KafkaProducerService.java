package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.config.MessagingConfig;
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
    
    public void sendMessageCommand(Object messageCommand) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageCommand);
            kafkaTemplate.send(messagingConfig.getMessageCommands(), jsonMessage);
            log.debug("Message command sent to topic: {}", messagingConfig.getMessageCommands());
        } catch (JsonProcessingException e) {
            log.error("Error serializing message command: {}", e.getMessage(), e);
        }
    }
    
    public void sendUserCommand(Object userCommand) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(userCommand);
            kafkaTemplate.send(messagingConfig.getUserCommands(), jsonMessage);
            log.debug("User command sent to topic: {}", messagingConfig.getUserCommands());
        } catch (JsonProcessingException e) {
            log.error("Error serializing user command: {}", e.getMessage(), e);
        }
    }
}
