package com.github.melihemreguler.messagingwriterservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {

    @Id
    private String id;

    private String username;
    private String ipAddress;
    private String userAgent;
    private boolean successful;
    private LocalDateTime timestamp;
    private String failureReason;
    private String action;

    public ActivityLogDto(String username, String ipAddress, String userAgent, boolean successful, String failureReason) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.successful = successful;
        this.failureReason = failureReason;
        this.timestamp = LocalDateTime.now();
        this.action = "LOGIN_ATTEMPT";
    }
}
