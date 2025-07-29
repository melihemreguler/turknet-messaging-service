package com.github.melihemreguler.messagingconsumer.enums;

import java.util.Optional;

public enum UserActivityCommand {
    USER_CREATION("USER_CREATION"),
    LOGIN_ATTEMPT("LOGIN_ATTEMPT");
    
    private final String value;
    
    UserActivityCommand(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static UserActivityCommand fromString(String value) {
        for (UserActivityCommand command : UserActivityCommand.values()) {
            if (command.getValue().equals(value)) {
                return command;
            }
        }
        throw new IllegalArgumentException("Unknown user activity command: " + value);
    }
    
    public static Optional<UserActivityCommand> fromStringOptional(String value) {
        for (UserActivityCommand command : UserActivityCommand.values()) {
            if (command.getValue().equals(value)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }
}
