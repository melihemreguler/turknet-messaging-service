package com.github.melihemreguler.messagingwriterservice.model;

import com.github.melihemreguler.messagingwriterservice.enums.UserCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationEvent {
    
    private String command;
    private String username;
    private String userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String timestamp;
}
