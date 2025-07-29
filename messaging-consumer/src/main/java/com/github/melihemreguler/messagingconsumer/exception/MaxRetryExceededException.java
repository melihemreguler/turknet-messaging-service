package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class MaxRetryExceededException extends BaseTurknetMessagingException {
    
    private final int maxRetryAttempts;
    private final Exception originalException;
    
    public MaxRetryExceededException(String originalMessage, int maxRetryAttempts, Exception originalException) {
        super(String.format("Max retry attempts (%d) exceeded for message processing", maxRetryAttempts), 
              "MAX_RETRY_EXCEEDED", originalMessage, originalException);
        this.maxRetryAttempts = maxRetryAttempts;
        this.originalException = originalException;
    }
    
    public MaxRetryExceededException(String originalMessage, int maxRetryAttempts, Exception originalException, String customMessage) {
        super(customMessage, "MAX_RETRY_EXCEEDED", originalMessage, originalException);
        this.maxRetryAttempts = maxRetryAttempts;
        this.originalException = originalException;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "MAX_RETRY_EXCEEDED";
    }
    
    @Override
    public String getExceptionType() {
        return "Retry Exhaustion";
    }
}
