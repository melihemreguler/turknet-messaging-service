package com.github.melihemreguler.turknetmessagingservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final KafkaAdmin kafkaAdmin;    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "turknet-messaging-service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
    
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "turknet-messaging-service");
        response.put("timestamp", LocalDateTime.now());

        Map<String, Object> checks = new HashMap<>();
        
        // Kafka health check with details
        Map<String, Object> kafkaStatus = checkKafkaHealth();
        checks.put("kafka", kafkaStatus);

        response.put("checks", checks);
        
        boolean allHealthy = checks.values().stream()
                .allMatch(check -> {
                    if (check instanceof Map) {
                        return "UP".equals(((Map<?, ?>) check).get("status"));
                    }
                    return false;
                });
        
        response.put("status", allHealthy ? "UP" : "DOWN");
        
        return allHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }

    private Map<String, Object> checkKafkaHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            ListTopicsOptions options = new ListTopicsOptions();
            options.timeoutMs(5000);
            
            Set<String> topicNames = adminClient.listTopics(options).names().get(5, TimeUnit.SECONDS);
            
            log.debug("Kafka health check successful. Found {} topics: {}", topicNames.size(), topicNames);
            
            result.put("status", "UP");
            result.put("broker", kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));
            result.put("topics", topicNames.size());
            result.put("topicNames", topicNames);
            
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            result.put("status", "DOWN");
            result.put("broker", kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"));
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
