package com.github.melihemreguler.turknetmessagingservice.enums;

public enum UserCommand {
    USER_CREATION("USER_CREATION"),
    LOGIN_ATTEMPT("LOGIN_ATTEMPT");

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
