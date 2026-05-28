package com.github.melihemreguler.turknetmessagingservice.model.response;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private String threadId;

    private String otherUserId;

    private String otherUsername;

    private MessageDto lastMessage;
}
