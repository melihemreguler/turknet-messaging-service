package com.github.melihemreguler.turknetmessagingservice.exception;

public class UserNotFoundException extends BaseTurknetMessagingException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    protected String getDefaultErrorCode() {
        return "USER_NOT_FOUND";
    }
    
    @Override
    public String getExceptionType() {
        return "UserNotFoundException";
    }
    
    public static UserNotFoundException forUserId(String userId) {
        return new UserNotFoundException("User not found: " + userId);
    }
    
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found: " + username);
    }
    
    public static UserNotFoundException forSender(String senderId) {
        return new UserNotFoundException("Sender not found: " + senderId);
    }
    
    public static UserNotFoundException forRecipient(String recipient) {
        return new UserNotFoundException("Recipient not found: " + recipient);
    }
    
    public static UserNotFoundException forConversation() {
        return new UserNotFoundException("One or both users not found");
    }
}
