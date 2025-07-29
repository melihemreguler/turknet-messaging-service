package com.github.melihemreguler.turknetmessagingservice.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HealthResponse(
        String status,
        String service,
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        LocalDateTime timestamp
) {
    public static HealthResponse up(String serviceName) {
        return new HealthResponse("UP", serviceName, LocalDateTime.now());
    }
    
    public static HealthResponse down(String serviceName) {
        return new HealthResponse("DOWN", serviceName, LocalDateTime.now());
    }
}
