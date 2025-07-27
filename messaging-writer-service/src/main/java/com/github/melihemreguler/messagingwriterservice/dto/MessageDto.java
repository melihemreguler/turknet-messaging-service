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
}
