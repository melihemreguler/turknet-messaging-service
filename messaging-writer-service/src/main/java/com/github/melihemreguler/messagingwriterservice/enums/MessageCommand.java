package com.github.melihemreguler.messagingwriterservice.enums;

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

    public static MessageCommand fromString(String command) {
        for (MessageCommand cmd : MessageCommand.values()) {
            if (cmd.getCommand().equals(command)) {
                return cmd;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + command);
    }
}
