package com.github.melihemreguler.turknetmessagingservice.model;

import com.github.melihemreguler.turknetmessagingservice.enums.UserCommand;
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
    
    public static UserCreationEvent create(String username, String userId, String email, 
                                         String ipAddress, String userAgent) {
        UserCreationEvent event = new UserCreationEvent();
        event.setCommand(UserCommand.CREATE_USER.getCommand());
        event.setUsername(username);
        event.setUserId(userId);
        event.setEmail(email);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        event.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return event;
    }
}
