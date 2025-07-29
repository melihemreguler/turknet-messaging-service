package com.github.melihemreguler.messagingconsumer.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNotFound(NoHandlerFoundException ex) {
        log.debug("404 Not Found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }
    
    @ExceptionHandler(MaxRetryExceededException.class)
    public void handleMaxRetryExceeded(MaxRetryExceededException ex) {
        log.error("Max retry attempts ({}) exceeded for message. Original error: {}", 
                 ex.getMaxRetryAttempts(), ex.getOriginalException().getMessage());
        log.error("Failed message: {}", ex.getOriginalMessage());
        
        // TODO: Send to dead letter queue or error logging system (Elasticsearch)
        // Here you can implement:
        // - Send to dead letter queue
        // - Log to Elasticsearch
        // - Send notification to monitoring system
        // - Store in error database table
        
        log.warn("Message marked as failed and needs manual intervention");
    }
    
    @ExceptionHandler(UnknownMessageCommandException.class)
    public void handleUnknownMessageCommand(UnknownMessageCommandException ex) {
        log.warn("Unknown message command received: {}", ex.getUnknownCommand());
        log.debug("Original message: {}", ex.getOriginalMessage());
        
        // TODO: You can implement additional handling here:
        // - Send to dead letter queue for unknown commands
        // - Log to monitoring system for analysis
        // - Create alerts for unknown command patterns
        // - Store in database for later investigation
        
        log.info("Unknown command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(UnknownSessionCommandException.class)
    public void handleUnknownSessionCommand(UnknownSessionCommandException ex) {
        log.warn("Unknown session command received: {}", ex.getUnknownCommand());
        log.debug("Original session message: {}", ex.getOriginalMessage());
        
        // TODO: You can implement additional handling here:
        // - Send to dead letter queue for unknown session commands
        // - Log to monitoring system for analysis
        // - Create alerts for unknown session command patterns
        // - Store in database for later investigation
        
        log.info("Unknown session command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(InvalidJsonFormatException.class)
    public void handleInvalidJsonFormat(InvalidJsonFormatException ex) {
        log.error("Invalid JSON format received: {}", ex.getMessage());
        log.debug("Invalid JSON content: {}", ex.getInvalidJson());
        
        // TODO: Handle malformed JSON messages
        // - Send to dead letter queue for malformed messages
        // - Log to monitoring system for analysis
        // - Create alerts for JSON parsing issues
        
        log.warn("Invalid JSON message rejected and logged for investigation");
    }
    
    @ExceptionHandler(UnknownUserActivityCommandException.class)
    public void handleUnknownUserActivityCommand(UnknownUserActivityCommandException ex) {
        log.warn("Unknown user activity command received: {}", ex.getUnknownCommand());
        log.debug("Original message: {}", ex.getOriginalMessage());
        
        // TODO: Handle unknown user activity commands
        // - Send to dead letter queue for unknown commands
        // - Log to monitoring system for analysis
        // - Create alerts for unknown command patterns
        
        log.info("Unknown user activity command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(UserActivityProcessingException.class)
    public void handleUserActivityProcessing(UserActivityProcessingException ex) {
        log.error("User activity processing failed: {}", ex.getMessage());
        log.debug("Failed user activity: {}", ex.getUserActivity());
        
        if (ex.getCause() != null) {
            log.error("Caused by: {}", ex.getCause().getMessage(), ex.getCause());
        }
        
        // TODO: Handle user activity processing failures
        // - Send to dead letter queue for retry or manual handling
        // - Log to monitoring system for analysis
        // - Create alerts for processing failures
        
        log.warn("User activity processing failure logged for investigation");
    }
    
    @ExceptionHandler(BaseTurknetMessagingException.class)
    public void handleBaseTurknetMessagingException(BaseTurknetMessagingException ex) {
        log.error("Turknet Messaging Service Error - Type: {}, Code: {}, Message: {}", 
                 ex.getExceptionType(), ex.getErrorCode(), ex.getMessage());
        
        if (ex.getOriginalMessage() != null) {
            log.debug("Original message: {}", ex.getOriginalMessage());
        }
        
        if (ex.getCause() != null) {
            log.error("Caused by: {}", ex.getCause().getMessage(), ex.getCause());
        }
        
        // TODO: Implement centralized handling for all Turknet messaging exceptions
        // - Send to appropriate dead letter queue based on error code
        // - Log to monitoring/alerting system
        // - Create metrics for error tracking
        // - Store in error database for analysis
        
        log.warn("Turknet messaging exception handled and logged for monitoring");
    }
}
