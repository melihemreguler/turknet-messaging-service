package com.github.melihemreguler.turknetmessagingservice.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull(message = "Recipient must not be null")
        @NotBlank(message = "Recipient cannot be empty")
        String recipient,
        
        @NotNull(message = "Content must not be null")
        @NotBlank(message = "Content cannot be empty")
        @Size(max = 1000, message = "Message content cannot exceed 1000 characters")
        String content
) {
    public MessageRequest {
        if (recipient != null) {
            recipient = recipient.trim().toLowerCase();
        }
        if (content != null) {
            content = content.trim();
        }
    }
    
    public String getTrimmedRecipient() {
        return recipient != null ? recipient.trim().toLowerCase() : null;
    }
    
    public String getTrimmedContent() {
        return content != null ? content.trim() : null;
    }
}
