package com.github.melihemreguler.messagingconsumer.dto;

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

    private String userId;
    private String ipAddress;
    private String userAgent;
    private boolean successful;
    private LocalDateTime timestamp;
    private String failureReason;
    private String action;
}
