package com.github.melihemreguler.turknetmessagingservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTopicInitializer {
    
    private final MessagingConfig messagingConfig;
    private final KafkaAdmin kafkaAdmin;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeTopics() {
        log.info("Initializing Kafka topics...");
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            
            // Create topics
            List<NewTopic> topics = List.of(
                TopicBuilder.name(messagingConfig.getUserCommands())
                    .partitions(3)
                    .replicas(1)
                    .build(),
                    
                TopicBuilder.name(messagingConfig.getMessageCommands())
                    .partitions(3)
                    .replicas(1)
                    .build(),
                    
                TopicBuilder.name(messagingConfig.getSessionCommands())
                    .partitions(3)
                    .replicas(1)
                    .build()
            );
            
            // Check if topics already exist
            var existingTopics = adminClient.listTopics().names().get();
            
            for (NewTopic topic : topics) {
                if (!existingTopics.contains(topic.name())) {
                    adminClient.createTopics(List.of(topic)).all().get();
                    log.info("Created Kafka topic: {}", topic.name());
                } else {
                    log.info("Kafka topic already exists: {}", topic.name());
                }
            }
            
            log.info("Kafka topics initialization completed successfully");
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to initialize Kafka topics: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error during Kafka topics initialization: {}", e.getMessage(), e);
        }
    }
}
