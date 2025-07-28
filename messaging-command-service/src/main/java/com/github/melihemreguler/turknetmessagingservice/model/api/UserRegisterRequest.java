package com.github.melihemreguler.turknetmessagingservice.model.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotNull(message = "Username must not be null")
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @NotNull(message = "Email must not be null")
        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Email should be valid")
        String email,
        
        @NotNull(message = "Password must not be null")
        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {
    public UserRegisterRequest {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
