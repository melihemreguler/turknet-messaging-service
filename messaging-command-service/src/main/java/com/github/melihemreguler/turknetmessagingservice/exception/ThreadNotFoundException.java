package com.github.melihemreguler.turknetmessagingservice.exception;

public class ThreadNotFoundException extends RuntimeException {
    public ThreadNotFoundException(String message) {
        super(message);
    }
    
    public static ThreadNotFoundException forUsers(String userId1, String userId2) {
        return new ThreadNotFoundException(String.format("Thread not found between users %s and %s", userId1, userId2));
    }
}
