package com.github.melihemreguler.turknetmessagingservice.enums;

public enum SessionCommand {
    SAVE_SESSION("SAVE_SESSION"),
    UPSERT_SESSION("UPSERT_SESSION"),
    UPDATE_SESSION("UPDATE_SESSION"),
    DELETE_SESSION("DELETE_SESSION"),
    EXPIRE_SESSION("EXPIRE_SESSION");

    private final String command;

    SessionCommand(String command) {
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
