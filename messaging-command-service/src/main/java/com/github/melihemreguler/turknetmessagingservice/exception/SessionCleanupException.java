package com.github.melihemreguler.turknetmessagingservice.exception;

public class SessionCleanupException extends BaseTurknetMessagingException {

    public SessionCleanupException(String message) {
        super(message, "SESSION_CLEANUP_ERROR");
    }

    public SessionCleanupException(String message, Throwable cause) {
        super(message, "SESSION_CLEANUP_ERROR", cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "SESSION_CLEANUP_ERROR";
    }

    @Override
    public String getExceptionType() {
        return "SessionCleanupException";
    }
}
