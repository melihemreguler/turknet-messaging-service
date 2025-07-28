package com.github.melihemreguler.turknetmessagingservice.exception;

public class MessageSerializationException extends RuntimeException {
    public MessageSerializationException(String message) {
        super(message);
    }
    
    public MessageSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
