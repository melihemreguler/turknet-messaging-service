package com.github.melihemreguler.turknetmessagingservice.enums;

public enum UserCommand {
    LOG_USER_ACTIVITY("LOG_USER_ACTIVITY"),
    CREATE_USER("CREATE_USER");

    private final String command;

    UserCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command;
    }
}
