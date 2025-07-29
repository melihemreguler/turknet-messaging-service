package com.github.melihemreguler.turknetmessagingservice.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String threadId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public MessageResponse(String threadId, String sender, String content) {
        this.threadId = threadId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
