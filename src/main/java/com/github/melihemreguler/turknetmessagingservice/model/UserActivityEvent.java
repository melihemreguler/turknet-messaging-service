package com.github.melihemreguler.turknetmessagingservice.model;

import java.time.LocalDateTime;

public record UserActivityEvent(
    String command,
    String username,
    String ipAddress,
    String userAgent,
    boolean successful,
    LocalDateTime timestamp,
    String failureReason
) {
    public static UserActivityEvent create(
            String username,
            String ipAddress,
            String userAgent,
            boolean successful,
            String failureReason) {
        return new UserActivityEvent(
            "LOG_USER_ACTIVITY",
            username,
            ipAddress,
            userAgent,
            successful,
            LocalDateTime.now(),
            failureReason
        );
    }
}
