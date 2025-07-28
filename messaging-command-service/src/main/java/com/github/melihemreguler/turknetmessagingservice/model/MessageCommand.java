package com.github.melihemreguler.turknetmessagingservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MessageCommand(
    String command,
    String messageId,
    String threadId,
    String sender,
    String content,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    LocalDateTime timestamp
) {
    public static MessageCommand create(
            String messageId,
            String threadId,
            String sender,
            String recipient,
            String content) {
        return new MessageCommand(
            com.github.melihemreguler.turknetmessagingservice.enums.MessageCommand.SEND_MESSAGE.getCommand(),
            messageId,
            threadId,
            sender,
            content,
            LocalDateTime.now()
        );
    }
}
