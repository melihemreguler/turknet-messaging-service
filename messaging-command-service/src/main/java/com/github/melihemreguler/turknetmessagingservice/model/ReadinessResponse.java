package com.github.melihemreguler.turknetmessagingservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record ReadinessResponse(
        String service,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime timestamp,
        
        Map<String, KafkaHealthStatus> checks,
        String status
) {
    public static ReadinessResponse create(String serviceName, KafkaHealthStatus kafkaStatus) {
        Map<String, KafkaHealthStatus> checks = Map.of("kafka", kafkaStatus);
        String overallStatus = kafkaStatus.status().equals("UP") ? "UP" : "DOWN";
        
        return new ReadinessResponse(
                serviceName,
                LocalDateTime.now(),
                checks,
                overallStatus
        );
    }
    
    public record KafkaHealthStatus(
            String status,
            String broker,
            Integer topics,
            Set<String> topicNames,
            String error
    ) {
        public static KafkaHealthStatus up(String broker, int topicCount, Set<String> topicNames) {
            return new KafkaHealthStatus("UP", broker, topicCount, topicNames, null);
        }
        
        public static KafkaHealthStatus down(String broker, String error) {
            return new KafkaHealthStatus("DOWN", broker, null, null, error);
        }
    }
}
