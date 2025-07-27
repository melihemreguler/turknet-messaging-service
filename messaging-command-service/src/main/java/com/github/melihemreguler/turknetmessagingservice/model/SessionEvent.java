package com.github.melihemreguler.turknetmessagingservice.model;

import java.time.LocalDateTime;

public record SessionEvent(
    String command,
    String sessionId,
    String hashedSessionToken,
    String userId,
    LocalDateTime expiresAt,
    String ipAddress,
    String userAgent,
    LocalDateTime timestamp
) {
    public static SessionEvent create(
            String hashedSessionToken,
            String userId,
            LocalDateTime expiresAt,
            String ipAddress,
            String userAgent) {
        return new SessionEvent(
            "SAVE_SESSION",
            null,
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
            "UPSERT_SESSION",
            null,
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
            "DELETE_USER_SESSIONS",
            null,
            null,
            userId,
            null,
            null,
            null,
            LocalDateTime.now()
        );
    }
}
