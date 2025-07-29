package com.github.melihemreguler.messagingconsumer.constants;

/**
 * Constants used across the messaging consumer application
 */
public final class KafkaConstants {
    
    /**
     * HTTP header name for retry count in Kafka messages
     */
    public static final String RETRY_COUNT_HEADER = "x-retryCount";
    
    private KafkaConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
