package com.github.melihemreguler.messagingwriterservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka.topics")
@Getter
@Setter
public class MessagingConfig {
    private String messageCommands;
    private String messageCommandsRetry;
    private String userCommands;
    private String userCommandsRetry;
    private String sessionCommands;
    private String sessionCommandsRetry;
}
