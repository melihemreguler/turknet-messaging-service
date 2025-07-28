package com.github.melihemreguler.turknetmessagingservice.enums;

import lombok.Getter;

/**
 * Constants for session and user management in HTTP requests/responses
 */
@Getter
public enum SessionConstants {
    
    /**
     * HTTP header name for session ID
     */
    SESSION_ID_HEADER("X-Session-Id"),
    
    /**
     * HTTP header name for user ID
     */
    USER_ID_HEADER("X-User-Id"),
    
    /**
     * Request attribute name for current user ID
     */
    USER_ID_ATTRIBUTE("currentUserId"),
    
    /**
     * Request attribute name for current username (if needed)
     */
    USERNAME_ATTRIBUTE("currentUser");
    
    private final String value;
    
    SessionConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
