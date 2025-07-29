package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingconsumer.dto.MessageDto;
import com.github.melihemreguler.messagingconsumer.model.MessageCommandEvent;
import com.github.melihemreguler.messagingconsumer.repository.MessageRepository;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.enums.MessageStatus;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import com.github.melihemreguler.messagingconsumer.exception.UnknownMessageCommandException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageConsumerService {
    
    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaRetryConfig retryConfig;
    private final ObjectMapper objectMapper;
    
    @Value("${app.kafka.topics.message-commands-retry}")
    private String messageCommandsRetryTopic;

    @KafkaListener(topics = "${app.kafka.topics.message-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageCommand(String message, 
                                    @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        log.info("Received message command: {}", message);
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            MessageCommandEvent event = objectMapper.readValue(message, MessageCommandEvent.class);
            
            if (isSendMessageCommand(event.getCommand())) {
                processMessageCommand(event);
            } else {
                throw new UnknownMessageCommandException(event.getCommand(), message);
            }
        } catch (Exception e) {
            log.error("Error processing message command: {}", e.getMessage(), e);
            handleRetry(message, retryCount, e);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.message-commands-retry}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessageCommandRetry(String message, 
                                         @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        log.info("Received retry message command: {} (retry: {})", message, retryCountHeader);
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            MessageCommandEvent event = objectMapper.readValue(message, MessageCommandEvent.class);
            
            if (isSendMessageCommand(event.getCommand())) {
                processMessageCommand(event);
            } else {
                throw new UnknownMessageCommandException(event.getCommand(), message);
            }
        } catch (Exception e) {
            log.error("Error processing retry message command: {}", e.getMessage(), e);
            handleRetry(message, retryCount, e);
        }
    }

    private void processMessageCommand(MessageCommandEvent event) {
        MessageDto message = MessageDto.builder()
                .threadId(event.getThreadId())
                .senderId(event.getSenderId())
                .senderUsername(event.getSenderUsername())
                .content(event.getContent())
                .timestamp(event.getTimestamp())
                .status(MessageStatus.SENT.getStatus())
                .build();

        MessageDto savedMessage = messageRepository.save(message);
        log.info("Message saved to database: {} from {} to recipient", 
                savedMessage.getId(), savedMessage.getSenderId());
    }
    
    private void handleRetry(String message, int retryCount, Exception error) {
        if (retryCount < retryConfig.getMaxRetry()) {
            int newRetryCount = retryCount + 1;
            log.warn("Retrying message processing (attempt {}/{})", newRetryCount, retryConfig.getMaxRetry());
            
            // Send to retry topic with incremented retry count
            ProducerRecord<String, String> retryRecord = new ProducerRecord<>(
                messageCommandsRetryTopic, message);
            retryRecord.headers().add(KafkaConstants.RETRY_COUNT_HEADER, 
                String.valueOf(newRetryCount).getBytes());
                
            kafkaTemplate.send(retryRecord)
                .thenAccept(result -> log.debug("Message sent to retry topic successfully"))
                .exceptionally(throwable -> {
                    log.error("Failed to send message to retry topic: {}", throwable.getMessage());
                    return null;
                });
        } else {
            throw new MaxRetryExceededException(message, retryConfig.getMaxRetry(), error);
        }
    }
    
    private boolean isSendMessageCommand(String command) {
        return com.github.melihemreguler.messagingconsumer.enums.MessageCommand.SEND_MESSAGE.getCommand().equals(command);
    }
}
