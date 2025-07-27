package com.github.melihemreguler.turknetmessagingservice.model;

import java.time.LocalDateTime;

public record UserActivityEvent(
    String command,
    String username,
    String userId,
    String email,
    String passwordHash,
    String ipAddress,
    String userAgent,
    boolean successful,
    LocalDateTime timestamp,
    String failureReason
) {
    public static UserActivityEvent create(
            String username,
            String userId,
            String ipAddress,
            String userAgent,
            boolean successful,
            String failureReason) {
        return new UserActivityEvent(
            "LOG_USER_ACTIVITY",
            username,
            userId,
            null,
            null,
            ipAddress,
            userAgent,
            successful,
            LocalDateTime.now(),
            failureReason
        );
    }
    
    public static UserActivityEvent createRegistration(
            String username,
            String userId,
            String email,
            String passwordHash,
            String ipAddress,
            String userAgent) {
        return new UserActivityEvent(
            "USER_REGISTERED",
            username,
            userId,
            email,
            passwordHash,
            ipAddress,
            userAgent,
            true,
            LocalDateTime.now(),
            null
        );
    }
}
