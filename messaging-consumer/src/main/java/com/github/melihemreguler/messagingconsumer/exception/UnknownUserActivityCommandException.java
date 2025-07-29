package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class UnknownUserActivityCommandException extends BaseTurknetMessagingException {
    
    private final String unknownCommand;
    
    public UnknownUserActivityCommandException(String unknownCommand, String originalMessage) {
        super(String.format("Unknown user activity command received: %s", unknownCommand), 
              "UNKNOWN_USER_ACTIVITY_COMMAND", originalMessage);
        this.unknownCommand = unknownCommand;
    }
    
    public UnknownUserActivityCommandException(String unknownCommand, String originalMessage, Throwable cause) {
        super(String.format("Unknown user activity command received: %s", unknownCommand), 
              "UNKNOWN_USER_ACTIVITY_COMMAND", originalMessage, cause);
        this.unknownCommand = unknownCommand;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "UNKNOWN_USER_ACTIVITY_COMMAND";
    }
    
    @Override
    public String getExceptionType() {
        return "Unknown User Activity Command";
    }
}
