package com.github.melihemreguler.turknetmessagingservice.model.api;

import jakarta.validation.constraints.Min;

public record ActivityLogsRequest(
        @Min(value = 1, message = "Limit must be at least 1")
        Integer limit,
        
        @Min(value = 0, message = "Offset must be at least 0")
        Integer offset
) {
    public ActivityLogsRequest {
        // Set default pagination values
        if (limit == null) {
            limit = 50;
        }
        if (offset == null) {
            offset = 0;
        }
    }
}
