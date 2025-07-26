package com.github.melihemreguler.turknetmessagingservice.model;

import java.util.Map;

public record ValidationErrorResponse(
    Map<String, String> errors
) {
    public static ValidationErrorResponse create(Map<String, String> errors) {
        return new ValidationErrorResponse(errors);
    }
}
