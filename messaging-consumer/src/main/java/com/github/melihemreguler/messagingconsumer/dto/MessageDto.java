package com.github.melihemreguler.messagingconsumer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@CompoundIndex(
        name = "threadId_1_timestamp_-1",
        def = "{'threadId': 1, 'timestamp': -1}"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    @Id
    private String id;

    private String threadId;
    private String senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime timestamp;
    private String status;
}
