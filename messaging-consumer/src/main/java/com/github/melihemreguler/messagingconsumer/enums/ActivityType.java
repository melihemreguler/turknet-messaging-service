package com.github.melihemreguler.messagingconsumer.enums;

public enum ActivityType {
    LOGIN_ATTEMPT("LOGIN_ATTEMPT"),
    USER_CREATION("USER_CREATION");
    
    private final String value;
    
    ActivityType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static ActivityType fromString(String value) {
        for (ActivityType type : ActivityType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown activity type: " + value);
    }
}
