package com.github.melihemreguler.messagingconsumer.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "sessions")
public class SessionDto {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String hashedSessionToken;
    
    @Indexed
    private String userId;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastAccessedAt;
    
    private String ipAddress;
    
    private String userAgent;
    
    // Constructors
    public SessionDto() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public SessionDto(String hashedSessionToken, String userId, LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this();
        this.hashedSessionToken = hashedSessionToken;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHashedSessionToken() {
        return hashedSessionToken;
    }
    
    public void setHashedSessionToken(String hashedSessionToken) {
        this.hashedSessionToken = hashedSessionToken;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SessionDto{" +
                "id='" + id + '\'' +
                ", hashedSessionToken='" + hashedSessionToken + '\'' +
                ", userId='" + userId + '\'' +
                ", expiresAt=" + expiresAt +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
