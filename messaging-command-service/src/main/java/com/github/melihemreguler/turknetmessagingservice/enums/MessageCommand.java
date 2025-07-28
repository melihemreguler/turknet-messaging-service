package com.github.melihemreguler.turknetmessagingservice.enums;

public enum MessageCommand {
    SEND_MESSAGE("SEND_MESSAGE");

    private final String command;

    MessageCommand(String command) {
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
