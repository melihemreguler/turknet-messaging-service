package com.github.melihemreguler.turknetmessagingservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record SessionEvent(
    String command,
    String hashedSessionToken,
    String userId,
    LocalDateTime expiresAt,
    String ipAddress,
    String userAgent,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    LocalDateTime timestamp
) {
    public static SessionEvent create(
            String hashedSessionToken,
            String userId,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent) {
        return new SessionEvent(
            com.github.melihemreguler.turknetmessagingservice.enums.SessionCommand.SAVE_SESSION.getCommand(),
            hashedSessionToken,
            userId,
            expiresAt,
            ipAddress,
            userAgent,
            LocalDateTime.now()
        );
    }
    
    public static SessionEvent createOrUpdate(
            String hashedSessionToken,
            String userId,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent) {
        return new SessionEvent(
            com.github.melihemreguler.turknetmessagingservice.enums.SessionCommand.UPSERT_SESSION.getCommand(),
            hashedSessionToken,
            userId,
            expiresAt,
            ipAddress,
            userAgent,
            LocalDateTime.now()
        );
    }

    public static SessionEvent deleteUserSessions(String userId) {
        return new SessionEvent(
            com.github.melihemreguler.turknetmessagingservice.enums.SessionCommand.DELETE_SESSION.getCommand(),
            null,
            userId,
            null,
            null,
            null,
            LocalDateTime.now()
        );
    }
}
