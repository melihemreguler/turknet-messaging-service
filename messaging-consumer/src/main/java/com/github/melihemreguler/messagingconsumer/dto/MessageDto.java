package com.github.melihemreguler.messagingconsumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    @Id
    private String id;

    private String threadId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String status;
}
