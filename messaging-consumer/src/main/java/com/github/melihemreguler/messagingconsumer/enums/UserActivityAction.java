package com.github.melihemreguler.messagingconsumer.enums;

import java.util.Optional;

public enum UserActivityAction {
    USER_CREATION("USER_CREATION"),
    LOGIN_SUCCESS("LOGIN_ATTEMPT"), // successful = true
    LOGIN_FAILURE("LOGIN_ATTEMPT"); // successful = false
    
    private final String value;
    
    UserActivityAction(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static UserActivityAction fromString(String value) {
        for (UserActivityAction action : UserActivityAction.values()) {
            if (action.getValue().equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown user activity action: " + value);
    }
    
    public static Optional<UserActivityAction> fromStringOptional(String value) {
        for (UserActivityAction action : UserActivityAction.values()) {
            if (action.getValue().equals(value)) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
    
    public boolean isLoginActivity() {
        return this == LOGIN_SUCCESS || this == LOGIN_FAILURE;
    }
    
    public boolean isUserCreation() {
        return this == USER_CREATION;
    }
    
    // Helper method to determine action from success status and type
    public static UserActivityAction fromLoginResult(boolean successful) {
        return successful ? LOGIN_SUCCESS : LOGIN_FAILURE;
    }
}
