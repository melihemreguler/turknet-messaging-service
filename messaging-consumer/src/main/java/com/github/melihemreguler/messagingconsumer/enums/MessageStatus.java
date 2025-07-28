package com.github.melihemreguler.messagingconsumer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageStatus {
    SENT("sent"),
    // TODO implement read status logic
    READ("read"),
    // TODO implement failed status logic
    FAILED("failed");
    
    private final String status;
    
    public static MessageStatus fromString(String status) {
        for (MessageStatus messageStatus : MessageStatus.values()) {
            if (messageStatus.getStatus().equalsIgnoreCase(status)) {
                return messageStatus;
            }
        }
        throw new IllegalArgumentException("Unknown message status: " + status);
    }
}
