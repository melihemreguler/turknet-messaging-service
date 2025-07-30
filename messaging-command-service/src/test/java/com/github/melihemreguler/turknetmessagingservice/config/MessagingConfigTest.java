package com.github.melihemreguler.turknetmessagingservice.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessagingConfigTest {
    @Test
    void givenDefaultConfig_whenGetProperties_thenReturnValues() {
        // Given
        MessagingConfig config = new MessagingConfig();
        // When
        config.setMessageCommands("msg-topic");
        config.setUserCommands("user-topic");
        config.setSessionCommands("session-topic");
        // Then
        assertEquals("msg-topic", config.getMessageCommands());
        assertEquals("user-topic", config.getUserCommands());
        assertEquals("session-topic", config.getSessionCommands());
    }
}
