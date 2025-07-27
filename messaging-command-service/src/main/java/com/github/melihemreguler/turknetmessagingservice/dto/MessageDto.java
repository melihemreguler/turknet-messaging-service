package com.github.melihemreguler.turknetmessagingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    @Id
    private String id;

    @Indexed
    private String threadId;

    @Indexed
    private String sender;

    private String content;

    private LocalDateTime timestamp;

    public MessageDto(String threadId, String sender, String content) {
        this.threadId = threadId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
