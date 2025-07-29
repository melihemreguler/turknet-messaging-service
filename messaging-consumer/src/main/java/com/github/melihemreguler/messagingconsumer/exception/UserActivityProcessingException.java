package com.github.melihemreguler.messagingconsumer.exception;

import lombok.Getter;

@Getter
public class UserActivityProcessingException extends BaseTurknetMessagingException {
    
    private final String userActivity;
    
    public UserActivityProcessingException(String userActivity, String message, Throwable cause) {
        super(String.format("Failed to process user activity: %s", message), 
              "USER_ACTIVITY_PROCESSING_ERROR", userActivity, cause);
        this.userActivity = userActivity;
    }
    
    public UserActivityProcessingException(String userActivity, Throwable cause) {
        super("Failed to process user activity", "USER_ACTIVITY_PROCESSING_ERROR", userActivity, cause);
        this.userActivity = userActivity;
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "USER_ACTIVITY_PROCESSING_ERROR";
    }
    
    @Override
    public String getExceptionType() {
        return "User Activity Processing Error";
    }
}
