package com.github.melihemreguler.turknetmessagingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
@Getter
@Setter
public class MessagingConfig {
    // Command topics for write operations
    private String userCommands;
    private String messageCommands;
    private String sessionCommands;
}
