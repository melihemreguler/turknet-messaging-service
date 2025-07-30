package com.github.melihemreguler.turknetmessagingservice.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {
    @Test
    void givenSecurityConfig_whenCreated_thenNotNull() {
        // Given & When
        SecurityConfig config = new SecurityConfig();
        // Then
        assertNotNull(config);
    }
}
