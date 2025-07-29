package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class InvalidJsonFormatException extends BaseTurknetMessagingException {
    
    private final String invalidJson;
    
    public InvalidJsonFormatException(String invalidJson, Throwable cause) {
        super("Failed to parse JSON message", "INVALID_JSON_FORMAT", invalidJson, cause);
        this.invalidJson = invalidJson;
    }
    
    public InvalidJsonFormatException(String invalidJson, String customMessage, Throwable cause) {
        super(customMessage, "INVALID_JSON_FORMAT", invalidJson, cause);
        this.invalidJson = invalidJson;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "INVALID_JSON_FORMAT";
    }
    
    @Override
    public String getExceptionType() {
        return "JSON Parsing Error";
    }
}
