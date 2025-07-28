package com.github.melihemreguler.turknetmessagingservice.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserActivityEvent(
    String command,
    String username,
    String userId,
    String ipAddress,
    String userAgent,
    boolean successful,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
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
            com.github.melihemreguler.turknetmessagingservice.enums.UserCommand.LOG_USER_ACTIVITY.getCommand(),
            username,
            userId,
            ipAddress,
            userAgent,
            successful,
            LocalDateTime.now(),
            failureReason
        );
    }
}
