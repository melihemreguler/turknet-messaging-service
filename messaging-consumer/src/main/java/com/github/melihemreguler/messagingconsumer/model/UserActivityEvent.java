package com.github.melihemreguler.messagingconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String ipAddress;
    private String userAgent;
    private boolean successful;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    private String failureReason;
    
    @JsonProperty("command")
    public String getCommand() {
        return command;
    }
}
