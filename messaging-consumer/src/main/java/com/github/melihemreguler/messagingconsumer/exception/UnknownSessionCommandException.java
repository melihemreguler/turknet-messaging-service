package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class UnknownSessionCommandException extends BaseTurknetMessagingException {
    
    private final String unknownCommand;
    
    public UnknownSessionCommandException(String unknownCommand, String originalMessage) {
        super(String.format("Unknown session command received: %s", unknownCommand), 
              "UNKNOWN_SESSION_COMMAND", originalMessage);
        this.unknownCommand = unknownCommand;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "UNKNOWN_SESSION_COMMAND";
    }
    
    @Override
    public String getExceptionType() {
        return "Unknown Command";
    }
}
