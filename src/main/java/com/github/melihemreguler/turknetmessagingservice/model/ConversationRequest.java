package com.github.melihemreguler.turknetmessagingservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConversationRequest(
        @NotNull(message = "User1 must not be null")
        @NotBlank(message = "User1 cannot be empty")
        String user1,
        
        @NotNull(message = "User2 must not be null")
        @NotBlank(message = "User2 cannot be empty")
        String user2
) {
    public ConversationRequest {
        if (user1 != null) {
            user1 = user1.trim().toLowerCase();
        }
        if (user2 != null) {
            user2 = user2.trim().toLowerCase();
        }
    }
}
