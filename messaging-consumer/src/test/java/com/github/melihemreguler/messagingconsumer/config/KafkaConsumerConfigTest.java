package com.github.melihemreguler.messagingconsumer.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = KafkaConsumerConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.group-id=test-group"
})
class KafkaConsumerConfigTest {
    @Autowired
    private KafkaConsumerConfig config;

    @Test
    void givenConfig_whenConsumerFactory_thenNotNull() {
        //GIVEN //WHEN
        ConsumerFactory<String, String> factory = config.consumerFactory();
        //THEN
        assertNotNull(factory);
    }

    @Test
    void givenConfig_whenKafkaListenerContainerFactory_thenNotNull() {
        //GIVEN //WHEN
        ConcurrentKafkaListenerContainerFactory<String, String> factory = config.kafkaListenerContainerFactory();
        //THEN
        assertNotNull(factory);
    }
}
