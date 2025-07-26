package com.github.melihemreguler.messagingwriterservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingwriterservice.dto.MessageDto;
import com.github.melihemreguler.messagingwriterservice.model.MessageCommandEvent;
import com.github.melihemreguler.messagingwriterservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumerService {

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(topics = "${app.kafka.topics.message-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageCommand(String message) {
        log.info("Received message command: {}", message);
        
        try {
            MessageCommandEvent event = objectMapper.readValue(message, MessageCommandEvent.class);
            
            if ("SEND_MESSAGE".equals(event.getCommand())) {
                processMessageCommand(event);
            } else {
                log.warn("Unknown message command: {}", event.getCommand());
            }
        } catch (Exception e) {
            log.error("Error processing message command: {}", e.getMessage(), e);
        }
    }

    private void processMessageCommand(MessageCommandEvent event) {
        MessageDto message = new MessageDto();
        message.setId(event.getMessageId());
        message.setThreadId(event.getThreadId());
        message.setSender(event.getSender());
        message.setRecipient(event.getRecipient());
        message.setContent(event.getContent());
        message.setTimestamp(event.getTimestamp());
        message.setStatus("delivered");

        MessageDto savedMessage = messageRepository.save(message);
        log.info("Message saved to database: {} from {} to {}", 
                savedMessage.getId(), savedMessage.getSender(), savedMessage.getRecipient());
    }
}
