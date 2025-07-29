package com.github.melihemreguler.turknetmessagingservice.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.melihemreguler.turknetmessagingservice.enums.UserCommand;

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
    String failureReason,
    String email // Optional field for user creation
) {
    
    // Factory method for login attempts
    public static UserActivityEvent createLoginAttempt(
            String username,
            String userId,
            String ipAddress,
            String userAgent,
            boolean successful,
            String failureReason) {
        return new UserActivityEvent(
            UserCommand.LOGIN_ATTEMPT.getCommand(),
            username,
            userId,
            ipAddress,
            userAgent,
            successful,
            LocalDateTime.now(),
            failureReason,
            null // No email for login attempts
        );
    }
    
    // Factory method for user creation
    public static UserActivityEvent createUserCreation(
            String username,
            String userId,
            String email,
            String ipAddress,
            String userAgent) {
        return new UserActivityEvent(
            UserCommand.USER_CREATION.getCommand(),
            username,
            userId,
            ipAddress,
            userAgent,
            true, // User creation is always successful
            LocalDateTime.now(),
            null, // No failure reason for successful creation
            email
        );
    }
}
