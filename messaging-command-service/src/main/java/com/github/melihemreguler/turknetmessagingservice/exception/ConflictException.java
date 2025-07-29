package com.github.melihemreguler.turknetmessagingservice.exception;

public class ConflictException extends BaseTurknetMessagingException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "CONFLICT_ERROR";
    }
    
    @Override
    public String getExceptionType() {
        return "ConflictException";
    }
}
