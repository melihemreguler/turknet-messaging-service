package com.github.melihemreguler.turknetmessagingservice.model.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

public record HistoryRequest(
        // User 1 ID (current user from interceptor - always required)
        String user1Id,
        
        // User 2 credentials (either userId or username required)
        String user2Id,
        String user2Username,
        
        // Pagination parameters
        @Min(value = 1, message = "Limit must be at least 1")
        Integer limit,
        
        @Min(value = 0, message = "Offset must be at least 0")
        Integer offset
) {
    public HistoryRequest {
        // Clean up the fields
        if (user1Id != null) {
            user1Id = user1Id.trim();
        }
        if (user2Id != null) {
            user2Id = user2Id.trim();
        }
        if (user2Username != null) {
            user2Username = user2Username.trim().toLowerCase();
        }
        
        // Set default pagination values
        if (limit == null) {
            limit = 50;
        }
        if (offset == null) {
            offset = 0;
        }
    }

    @AssertTrue(message = "User1 ID is required")
    public boolean isUser1Valid() {
        return user1Id != null && !user1Id.trim().isEmpty();
    }

    @AssertTrue(message = "User2 must have either userId or username")
    public boolean isUser2Valid() {
        return (user2Id != null && !user2Id.trim().isEmpty()) ||
               (user2Username != null && !user2Username.trim().isEmpty());
    }
    
    // Helper methods to get the primary identifier for each user
    public String getUser1PrimaryId() {
        return user1Id;
    }
    
    public String getUser2PrimaryId() {
        return user2Id != null && !user2Id.trim().isEmpty() ? user2Id : user2Username;
    }
    
    public boolean isUser1ByUserId() {
        return true; // Always true since user1 is always identified by userId
    }
    
    public boolean isUser2ByUserId() {
        return user2Id != null && !user2Id.trim().isEmpty();
    }
}
