package com.github.melihemreguler.turknetmessagingservice.model;

import java.time.LocalDateTime;

public record MessageCommand(
    String command,
    String messageId,
    String threadId,
    String sender,
    String recipient,
    String content,
    LocalDateTime timestamp,
    String status
) {
    public static MessageCommand create(
            String messageId,
            String threadId,
            String sender,
            String recipient,
            String content) {
        return new MessageCommand(
            "SEND_MESSAGE",
            messageId,
            threadId,
            sender,
            recipient,
            content,
            LocalDateTime.now(),
            "pending"
        );
    }
}
