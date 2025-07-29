package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

/**
 * Base exception class for all Turknet Messaging Service related exceptions.
 * All custom exceptions in the messaging consumer service should extend this class.
 */
@Getter
public abstract class BaseTurknetMessagingException extends RuntimeException {
    
    private final String errorCode;
    private final String originalMessage;
    
    protected BaseTurknetMessagingException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
        this.originalMessage = null;
    }
    
    protected BaseTurknetMessagingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.originalMessage = null;
    }
    
    protected BaseTurknetMessagingException(String message, String errorCode, String originalMessage) {
        super(message);
        this.errorCode = errorCode;
        this.originalMessage = originalMessage;
    }
    
    protected BaseTurknetMessagingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
        this.originalMessage = null;
    }
    
    protected BaseTurknetMessagingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.originalMessage = null;
    }
    
    protected BaseTurknetMessagingException(String message, String errorCode, String originalMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.originalMessage = originalMessage;
    }
    
    /**
     * Returns the default error code for this exception type.
     * Should be overridden by subclasses to provide specific error codes.
     */
    protected abstract String getDefaultErrorCode();
    
    /**
     * Returns a short description of the exception type.
     * Used for logging and monitoring purposes.
     */
    public abstract String getExceptionType();
}
