package com.github.melihemreguler.turknetmessagingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    @Id
    private String id;

    @Indexed(unique = true)
    private String hashedSessionId;

    private String userId;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private String ipAddress;

    private String userAgent;

    public SessionDto(String hashedSessionId, String userId,
                      LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this.hashedSessionId = hashedSessionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
