package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.model.api.HealthResponse;
import com.github.melihemreguler.turknetmessagingservice.model.api.ReadinessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final KafkaAdmin kafkaAdmin;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.up("turknet-messaging-service"));
    }
    
    
    @GetMapping("/ready")
    public ResponseEntity<ReadinessResponse> readiness() {
        ReadinessResponse.KafkaHealthStatus kafkaStatus = checkKafkaHealth();
        ReadinessResponse response = ReadinessResponse.create("turknet-messaging-service", kafkaStatus);
        
        boolean isHealthy = "UP".equals(response.status());
        return isHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }

    private ReadinessResponse.KafkaHealthStatus checkKafkaHealth() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsOptions options = new ListTopicsOptions();
            options.timeoutMs(5000);
            
            Set<String> topicNames = adminClient.listTopics(options).names().get(5, TimeUnit.SECONDS);
            
            log.debug("Kafka health check successful. Found {} topics: {}", topicNames.size(), topicNames);
            
            String broker = (String) kafkaAdmin.getConfigurationProperties().get("bootstrap.servers");
            return ReadinessResponse.KafkaHealthStatus.up(broker, topicNames.size(), topicNames);
            
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            String broker = (String) kafkaAdmin.getConfigurationProperties().get("bootstrap.servers");
            return ReadinessResponse.KafkaHealthStatus.down(broker, e.getMessage());
        }
    }
}
