package com.github.melihemreguler.turknetmessagingservice.logging;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogDocumentBuilderTest {
    @Test
    void givenLogDocumentBuilder_whenBuild_thenReturnsNotNull() {
        // Given
        long timestamp = System.currentTimeMillis();
        String level = "INFO";
        String message = "Test message";
        String loggerName = "TestLogger";
        String threadName = "main";
        String application = "turknet-messaging-service";
        String service = "test-service";

        // When
        LogDocumentBuilder builder = LogDocumentBuilder.create()
                .withTimestamp(timestamp)
                .withLevel(level)
                .withMessage(message)
                .withLoggerName(loggerName)
                .withThreadName(threadName)
                .withApplication(application)
                .withService(service);
        Object result = builder.build();

        // Then
        assertNotNull(result);
        assertTrue(result instanceof java.util.Map);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
        assertEquals(application, map.get("application"));
        assertEquals(service, map.get("service"));
        assertEquals(level, map.get("level"));
        assertEquals(message, map.get("message"));
        assertEquals(loggerName, map.get("logger_name"));
        assertEquals(threadName, map.get("thread_name"));
    }
}
