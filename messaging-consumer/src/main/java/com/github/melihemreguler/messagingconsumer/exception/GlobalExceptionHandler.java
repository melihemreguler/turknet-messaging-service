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
        
    }
    
    @ExceptionHandler(UnknownMessageCommandException.class)
    public void handleUnknownMessageCommand(UnknownMessageCommandException ex) {
        log.warn("Unknown message command received: {}", ex.getUnknownCommand());
        log.debug("Original message: {}", ex.getOriginalMessage());
        
        log.info("Unknown command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(UnknownSessionCommandException.class)
    public void handleUnknownSessionCommand(UnknownSessionCommandException ex) {
        log.warn("Unknown session command received: {}", ex.getUnknownCommand());
        log.debug("Original session message: {}", ex.getOriginalMessage());
        
        log.info("Unknown session command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(InvalidJsonFormatException.class)
    public void handleInvalidJsonFormat(InvalidJsonFormatException ex) {
        log.error("Invalid JSON format received: {}", ex.getMessage());
        log.debug("Invalid JSON content: {}", ex.getInvalidJson());
        
        log.warn("Invalid JSON message rejected and logged for investigation");
    }
    
    @ExceptionHandler(UnknownUserActivityCommandException.class)
    public void handleUnknownUserActivityCommand(UnknownUserActivityCommandException ex) {
        log.warn("Unknown user activity command received: {}", ex.getUnknownCommand());
        log.debug("Original message: {}", ex.getOriginalMessage());
        
        log.info("Unknown user activity command message ignored and logged for investigation");
    }
    
    @ExceptionHandler(UserActivityProcessingException.class)
    public void handleUserActivityProcessing(UserActivityProcessingException ex) {
        log.error("User activity processing failed: {}", ex.getMessage());
        log.debug("Failed user activity: {}", ex.getUserActivity());
        
        if (ex.getCause() != null) {
            log.error("Caused by: {}", ex.getCause().getMessage(), ex.getCause());
        }

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
        
        log.warn("Turknet messaging exception handled and logged for monitoring");
    }
}
