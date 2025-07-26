package com.github.melihemreguler.turknetmessagingservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotNull(message = "Username must not be null")
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,
        
        @NotNull(message = "Password must not be null")
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {
    public UserRegistrationRequest {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
    }
    
    public String getTrimmedUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }
}
