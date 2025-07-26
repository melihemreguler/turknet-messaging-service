package com.github.melihemreguler.messagingwriterservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    @Id
    private String id;

    private String threadId;
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;
    private String status;

    public MessageDto(String threadId, String sender, String content) {
        this.threadId = threadId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = "sent";
    }

    public MessageDto(String threadId, String sender, String recipient, String content) {
        this.threadId = threadId;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = "sent";
    }
}
