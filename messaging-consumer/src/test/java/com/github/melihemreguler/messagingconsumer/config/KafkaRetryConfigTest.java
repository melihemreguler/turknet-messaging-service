package com.github.melihemreguler.messagingconsumer.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KafkaRetryConfigTest {
    @Test
    void givenConfig_whenSetAndGetMaxRetry_thenReturnsValue() {
        //GIVEN
        KafkaRetryConfig config = new KafkaRetryConfig();
        config.setMaxRetry(5);
        //WHEN
        int maxRetry = config.getMaxRetry();
        //THEN
        assertEquals(5, maxRetry);
    }

    @Test
    void givenConfig_whenSetAndGetCron_thenReturnsValue() {
        //GIVEN
        KafkaRetryConfig config = new KafkaRetryConfig();
        config.setCron("0 0 * * * *");
        //WHEN
        String cron = config.getCron();
        //THEN
        assertEquals("0 0 * * * *", cron);
    }
}
