package com.github.melihemreguler.turknetmessagingservice.enums;

/**
 * Constants for session and user management in HTTP requests/responses
 */
public final class SessionConstants {
    
    /**
     * HTTP header name for session ID
     */
    public static final String SESSION_ID_HEADER = "X-Session-Id";
    
    /**
     * HTTP header name for user ID
     */
    public static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * Request attribute name for current user ID
     */
    public static final String USER_ID_ATTRIBUTE = "currentUserId";
    
    /**
     * Request attribute name for current username (if needed)
     */
    public static final String USERNAME_ATTRIBUTE = "currentUser";
    
    /**
     * HTTP header name for X-Forwarded-For
     */
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    
    /**
     * HTTP header name for X-Real-IP
     */
    public static final String X_REAL_IP_HEADER = "X-Real-IP";
    
    /**
     * HTTP header name for User-Agent
     */
    public static final String USER_AGENT_HEADER = "User-Agent";
    
    private SessionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
