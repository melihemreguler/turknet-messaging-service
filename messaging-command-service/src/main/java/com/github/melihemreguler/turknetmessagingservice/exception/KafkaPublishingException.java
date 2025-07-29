package com.github.melihemreguler.turknetmessagingservice.exception;

public class KafkaPublishingException extends BaseTurknetMessagingException {
    
    public KafkaPublishingException(String message) {
        super(message);
    }
    
    public KafkaPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "KAFKA_PUBLISHING_ERROR";
    }
    
    @Override
    public String getExceptionType() {
        return "KafkaPublishingException";
    }
}
