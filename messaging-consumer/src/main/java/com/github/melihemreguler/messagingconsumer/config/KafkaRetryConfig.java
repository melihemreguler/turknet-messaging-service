package com.github.melihemreguler.messagingconsumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.retry")
@Getter
@Setter
public class KafkaRetryConfig {
    private int maxRetry = 5;
    private String cron = "*/10 * * * *";
}
