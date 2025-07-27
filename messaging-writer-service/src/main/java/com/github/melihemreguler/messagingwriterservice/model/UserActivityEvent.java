package com.github.melihemreguler.messagingwriterservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityEvent {
    
    private String command;
    private String username;
    private String userId;
    private String email;
    private String passwordHash;
    private String ipAddress;
    private String userAgent;
    private boolean successful;
    private LocalDateTime timestamp;
    private String failureReason;
    
    @JsonProperty("command")
    public String getCommand() {
        return command;
    }
}
