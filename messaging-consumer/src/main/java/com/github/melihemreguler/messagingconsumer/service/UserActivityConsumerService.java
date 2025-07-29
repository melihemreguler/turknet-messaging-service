package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.enums.UserActivityCommand;
import com.github.melihemreguler.messagingconsumer.exception.InvalidJsonFormatException;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import com.github.melihemreguler.messagingconsumer.exception.UnknownUserActivityCommandException;
import com.github.melihemreguler.messagingconsumer.exception.UserActivityProcessingException;
import com.github.melihemreguler.messagingconsumer.model.UserActivityEvent;
import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategy;
import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityConsumerService {

    private final ObjectMapper objectMapper;
    private final UserActivityStrategyFactory strategyFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaRetryConfig retryConfig;
    
    @Value("${app.kafka.topics.user-commands-retry}")
    private String userCommandsRetryTopic;

    @KafkaListener(topics = "${app.kafka.topics.user-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivity(String message,
                                   @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        log.info("Received user activity: {}", message);
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
            
            UserActivityCommand command = UserActivityCommand.fromString(event.getCommand());
            
            UserActivityStrategy strategy = strategyFactory.getStrategy(command);
            if (strategy != null) {
                strategy.execute(event);
            } else {
                log.error("No strategy found for command: {}", command);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse user activity JSON: {}", message, e);
            handleRetry(message, retryCount, e);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown user activity command in message: {}", message);
            handleRetry(message, retryCount, e);
        } catch (Exception e) {
            log.error("Error processing user activity: {}", message, e);
            handleRetry(message, retryCount, e);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.user-commands-retry}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivityRetry(String message,
                                        @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        log.info("Received retry user activity: {} (retry: {})", message, retryCountHeader);
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
            
            UserActivityCommand command = UserActivityCommand.fromString(event.getCommand());
            
            UserActivityStrategy strategy = strategyFactory.getStrategy(command);
            if (strategy != null) {
                strategy.execute(event);
            } else {
                log.error("No strategy found for command: {}", command);
            }
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse retry user activity JSON: {}", message, e);
            handleRetry(message, retryCount, e);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown user activity command in retry message: {}", message);
            handleRetry(message, retryCount, e);
        } catch (Exception e) {
            log.error("Error processing retry user activity: {}", message, e);
            handleRetry(message, retryCount, e);
        }
    }
    
    private void handleRetry(String message, int retryCount, Exception error) {
        if (retryCount < retryConfig.getMaxRetry()) {
            int newRetryCount = retryCount + 1;
            log.warn("Retrying user activity processing (attempt {}/{})", newRetryCount, retryConfig.getMaxRetry());
            
            ProducerRecord<String, String> retryRecord = new ProducerRecord<>(
                userCommandsRetryTopic, message);
            retryRecord.headers().add(KafkaConstants.RETRY_COUNT_HEADER, 
                String.valueOf(newRetryCount).getBytes());
                
            kafkaTemplate.send(retryRecord)
                .thenAccept(result -> log.debug("User activity message sent to retry topic successfully"))
                .exceptionally(throwable -> {
                    log.error("Failed to send user activity message to retry topic: {}", throwable.getMessage());
                    return null;
                });
        } else {
            throw new MaxRetryExceededException(message, retryConfig.getMaxRetry(), error);
        }
    }
}
