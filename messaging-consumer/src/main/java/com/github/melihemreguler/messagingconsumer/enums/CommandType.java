package com.github.melihemreguler.messagingconsumer.enums;

public enum CommandType {
    CREATE_USER("CREATE_USER"),
    LOG_USER_ACTIVITY("LOG_USER_ACTIVITY");
    
    private final String value;
    
    CommandType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static CommandType fromString(String value) {
        for (CommandType type : CommandType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown command type: " + value);
    }
}
