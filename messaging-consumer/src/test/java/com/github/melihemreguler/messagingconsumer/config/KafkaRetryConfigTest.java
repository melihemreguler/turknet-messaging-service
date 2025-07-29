package com.github.melihemreguler.messagingconsumer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KafkaRetryConfigTest {

    @Autowired
    private KafkaRetryConfig kafkaRetryConfig;

    @Test
    void testKafkaRetryConfigDefaultProfile() {
        assertNotNull(kafkaRetryConfig);
        assertEquals(5, kafkaRetryConfig.getMaxRetry(), 
                "Max retry should be 5 for default/local profile");
        assertEquals("*/10 * * * *", kafkaRetryConfig.getCron(), 
                "Cron should be '*/10 * * * *' for default/local profile");
    }

    @SpringBootTest
    @ActiveProfiles("staging")
    static class StagingProfileTest {
        
        @Autowired
        private KafkaRetryConfig kafkaRetryConfig;

        @Test
        void testKafkaRetryConfigStagingProfile() {
            assertNotNull(kafkaRetryConfig);
            assertEquals(5, kafkaRetryConfig.getMaxRetry(), 
                    "Max retry should be 5 for staging profile");
            assertEquals("*/10 * * * *", kafkaRetryConfig.getCron(), 
                    "Cron should be '*/10 * * * *' for staging profile");
        }
    }

    @SpringBootTest
    @ActiveProfiles("prod")
    static class ProdProfileTest {
        
        @Autowired
        private KafkaRetryConfig kafkaRetryConfig;

        @Test
        void testKafkaRetryConfigProdProfile() {
            assertNotNull(kafkaRetryConfig);
            assertEquals(3, kafkaRetryConfig.getMaxRetry(), 
                    "Max retry should be 3 for prod profile");
            assertEquals("*/30 * * * *", kafkaRetryConfig.getCron(), 
                    "Cron should be '*/30 * * * *' for prod profile");
        }
    }
}
