package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.melihemreguler.messagingconsumer.config.KafkaRetryConfig;
import com.github.melihemreguler.messagingconsumer.constants.KafkaConstants;
import com.github.melihemreguler.messagingconsumer.model.SessionEvent;
import com.github.melihemreguler.messagingconsumer.enums.SessionCommand;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategy;
import com.github.melihemreguler.messagingconsumer.strategy.session.SessionCommandStrategyFactory;
import com.github.melihemreguler.messagingconsumer.exception.MaxRetryExceededException;
import com.github.melihemreguler.messagingconsumer.exception.UnknownSessionCommandException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.messaging.handler.annotation.Header;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionConsumerService {
    
    private final ObjectMapper objectMapper;
    private final SessionCommandStrategyFactory strategyFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaRetryConfig retryConfig;
    
    @Value("${app.kafka.topics.session-commands-retry}")
    private String sessionCommandsRetryTopic;
    
    @KafkaListener(topics = "${app.kafka.topics.session-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSessionEvent(String message,
                                  @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            log.info("Received session event message: {}", message);
            
            SessionEvent sessionEvent = objectMapper.readValue(message, SessionEvent.class);
            
            log.info("Parsed session event: {} for user: {}", 
                     sessionEvent.getCommand(), sessionEvent.getUserId());
            
            processSessionCommand(sessionEvent, message);
            
            log.debug("Successfully processed session event: {}", sessionEvent.getCommand());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse session event JSON: {}", message, e);
            handleRetry(message, retryCount, e);
        } catch (UnknownSessionCommandException e) {
            log.warn("Unknown session command in message: {}", message);
            handleRetry(message, retryCount, e);
        } catch (Exception e) {
            log.error("Unexpected error processing session event: {}", message, e);
            handleRetry(message, retryCount, e);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.session-commands-retry}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSessionEventRetry(String message,
                                       @Header(value = KafkaConstants.RETRY_COUNT_HEADER, defaultValue = "0") String retryCountHeader) {
        log.info("Received retry session event message: {} (retry: {})", message, retryCountHeader);
        
        int retryCount = Integer.parseInt(retryCountHeader);
        
        try {
            SessionEvent sessionEvent = objectMapper.readValue(message, SessionEvent.class);
            
            log.info("Parsed retry session event: {} for user: {}", 
                     sessionEvent.getCommand(), sessionEvent.getUserId());
            
            processSessionCommand(sessionEvent, message);
            
            log.debug("Successfully processed retry session event: {}", sessionEvent.getCommand());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse retry session event JSON: {}", message, e);
            handleRetry(message, retryCount, e);
        } catch (UnknownSessionCommandException e) {
            log.warn("Unknown session command in retry message: {}", message);
            handleRetry(message, retryCount, e);
        } catch (Exception e) {
            log.error("Unexpected error processing retry session event: {}", message, e);
            handleRetry(message, retryCount, e);
        }
    }
    
    private void processSessionCommand(SessionEvent sessionEvent, String originalMessage) 
            throws UnknownSessionCommandException {
        
        Optional<SessionCommand> commandOpt = SessionCommand.fromStringOptional(sessionEvent.getCommand());
        
        if (commandOpt.isEmpty()) {
            throw new UnknownSessionCommandException(sessionEvent.getCommand(), originalMessage);
        }
        
        SessionCommand command = commandOpt.get();
        SessionCommandStrategy strategy = strategyFactory.getStrategy(command);
        
        if (strategy != null) {
            strategy.execute(sessionEvent);
        } else {
            log.error("No strategy found for command: {}", command);
        }
    }
    
    private void handleRetry(String message, int retryCount, Exception error) {
        if (retryCount < retryConfig.getMaxRetry()) {
            int newRetryCount = retryCount + 1;
            log.warn("Retrying session processing (attempt {}/{})", newRetryCount, retryConfig.getMaxRetry());
            
            ProducerRecord<String, String> retryRecord = new ProducerRecord<>(
                sessionCommandsRetryTopic, message);
            retryRecord.headers().add(KafkaConstants.RETRY_COUNT_HEADER, 
                String.valueOf(newRetryCount).getBytes());
                
            kafkaTemplate.send(retryRecord)
                .thenAccept(result -> log.debug("Session message sent to retry topic successfully"))
                .exceptionally(throwable -> {
                    log.error("Failed to send session message to retry topic: {}", throwable.getMessage());
                    return null;
                });
        } else {
            throw new MaxRetryExceededException(message, retryConfig.getMaxRetry(), error);
        }
    }
}
