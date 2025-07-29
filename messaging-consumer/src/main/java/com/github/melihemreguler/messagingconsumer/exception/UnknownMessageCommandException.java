package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class UnknownMessageCommandException extends BaseTurknetMessagingException {
    
    private final String unknownCommand;
    
    public UnknownMessageCommandException(String unknownCommand, String originalMessage) {
        super(String.format("Unknown message command received: %s", unknownCommand), 
              "UNKNOWN_MESSAGE_COMMAND", originalMessage);
        this.unknownCommand = unknownCommand;
    }
    
    public UnknownMessageCommandException(String unknownCommand, String originalMessage, String customMessage) {
        super(customMessage, "UNKNOWN_MESSAGE_COMMAND", originalMessage);
        this.unknownCommand = unknownCommand;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "UNKNOWN_MESSAGE_COMMAND";
    }
    
    @Override
    public String getExceptionType() {
        return "Unknown Command";
    }
}
