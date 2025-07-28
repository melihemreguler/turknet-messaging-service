package com.github.melihemreguler.messagingconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCommandEvent {
    
    private String command;
    private String threadId;
    private String senderId;
    private String senderUsername;
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    @JsonProperty("command")
    public String getCommand() {
        return command;
    }
}
