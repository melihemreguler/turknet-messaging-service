package com.github.melihemreguler.turknetmessagingservice.exception;

public class MessageSerializationException extends BaseTurknetMessagingException {
    
    public MessageSerializationException(String message) {
        super(message);
    }
    
    public MessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "MESSAGE_SERIALIZATION_ERROR";
    }
    
    @Override
    public String getExceptionType() {
        return "MessageSerializationException";
    }
}
