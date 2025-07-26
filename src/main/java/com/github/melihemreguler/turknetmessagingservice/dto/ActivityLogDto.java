package com.github.melihemreguler.turknetmessagingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {

    @Id
    private String id;

    @Indexed
    private String username;

    private String ipAddress;

    private String userAgent;

    private boolean successful;

    private LocalDateTime timestamp;

    private String failureReason;

    public ActivityLogDto(String username, String ipAddress, String userAgent, boolean successful) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.successful = successful;
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLogDto(String username, String ipAddress, String userAgent, boolean successful, String failureReason) {
        this(username, ipAddress, userAgent, successful);
        this.failureReason = failureReason;
    }
}
