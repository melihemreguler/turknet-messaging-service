package com.github.melihemreguler.turknetmessagingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {

    @Id
    private String id;

    @Indexed
    private String userId;

    private List<ActivityEntry> logs = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEntry {
        private String ipAddress;
        private String userAgent;
        private boolean successful;
        private LocalDateTime timestamp;
        private String failureReason;
        private String action;
        
        public ActivityEntry(String ipAddress, String userAgent, boolean successful, LocalDateTime timestamp) {
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.successful = successful;
            this.timestamp = timestamp;
        }
    }
    
    public ActivityLogDto(String userId) {
        this.userId = userId;
    }
    
    public void addActivity(String ipAddress, String userAgent, boolean successful, LocalDateTime timestamp, String failureReason) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(new ActivityEntry(ipAddress, userAgent, successful, timestamp, failureReason, "LOGIN_ATTEMPT"));
    }
    
    public void addActivity(String ipAddress, String userAgent, boolean successful, LocalDateTime timestamp, String failureReason, String action) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(new ActivityEntry(ipAddress, userAgent, successful, timestamp, failureReason, action));
    }
}
