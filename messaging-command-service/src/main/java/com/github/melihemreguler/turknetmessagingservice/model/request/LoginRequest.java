package com.github.melihemreguler.turknetmessagingservice.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Username must not be null")
        @NotBlank(message = "Username cannot be empty")
        String username,
        
        @NotNull(message = "Password must not be null")
        @NotBlank(message = "Password cannot be empty")
        String password
) {
    public LoginRequest {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
    }
    
    public String getTrimmedUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }
}
