package com.github.melihemreguler.messagingwriterservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEvent {
    
    @JsonProperty("command")
    private String command;
    
    @JsonProperty("hashedSessionToken")
    private String hashedSessionToken;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("expiresAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime expiresAt;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("userAgent")
    private String userAgent;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
}
