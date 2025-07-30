package com.github.melihemreguler.messagingconsumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = JsonConfig.class)
class JsonConfigTest {
    @Autowired
    private JsonConfig jsonConfig;

    @Test
    void givenJsonConfig_whenObjectMapperBean_thenConfiguredCorrectly() {
        //GIVEN //WHEN
        ObjectMapper mapper = jsonConfig.objectMapper();
        //THEN
        assertNotNull(mapper);
        assertFalse(mapper.getSerializationConfig().isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }
}
