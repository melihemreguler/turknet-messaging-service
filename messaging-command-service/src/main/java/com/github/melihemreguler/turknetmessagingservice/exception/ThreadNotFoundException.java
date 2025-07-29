package com.github.melihemreguler.turknetmessagingservice.exception;

public class ThreadNotFoundException extends BaseTurknetMessagingException {
    
    public ThreadNotFoundException(String message) {
        super(message);
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "THREAD_NOT_FOUND";
    }
    
    @Override
    public String getExceptionType() {
        return "ThreadNotFoundException";
    }
    
    public static ThreadNotFoundException forUsers(String userId1, String userId2) {
        return new ThreadNotFoundException(String.format("Thread not found between users %s and %s", userId1, userId2));
    }
}
