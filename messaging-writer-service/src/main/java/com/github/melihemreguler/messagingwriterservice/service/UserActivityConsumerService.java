package com.github.melihemreguler.messagingwriterservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingwriterservice.dto.ActivityLogDto;
import com.github.melihemreguler.messagingwriterservice.dto.UserDto;
import com.github.melihemreguler.messagingwriterservice.model.UserActivityEvent;
import com.github.melihemreguler.messagingwriterservice.model.UserCreationEvent;
import com.github.melihemreguler.messagingwriterservice.repository.ActivityLogRepository;
import com.github.melihemreguler.messagingwriterservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class UserActivityConsumerService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserActivityConsumerService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "${app.kafka.topics.user-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivity(String message) {
        log.info("Received user activity: {}", message);
        
        try {
            // First try to parse as UserCreationEvent for CREATE_USER command
            if (message.contains("CREATE_USER")) {
                UserCreationEvent creationEvent = objectMapper.readValue(message, UserCreationEvent.class);
                processUserCreationEvent(creationEvent);
                return;
            }
            
            // Parse as UserActivityEvent for other commands
            UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
            
            switch (event.getCommand()) {
                case "LOG_USER_ACTIVITY" -> processUserActivityEvent(event);
                default -> {
                    log.warn("Unknown user activity command: {}", event.getCommand());
                    // Try to parse with enum for validation
                    try {
                        com.github.melihemreguler.messagingwriterservice.enums.UserCommand.fromString(event.getCommand());
                    } catch (IllegalArgumentException e2) {
                        log.error("Command not found in enum: {}", event.getCommand());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing user activity: {}", e.getMessage(), e);
        }
    }

    private void processUserActivityEvent(UserActivityEvent event) {
        ActivityLogDto activityLog = new ActivityLogDto();
        activityLog.setUserId(event.getUserId());
        activityLog.setIpAddress(event.getIpAddress());
        activityLog.setUserAgent(event.getUserAgent());
        activityLog.setSuccessful(event.isSuccessful());
        activityLog.setTimestamp(event.getTimestamp());
        activityLog.setFailureReason(event.getFailureReason());
        activityLog.setAction("LOGIN_ATTEMPT");

        ActivityLogDto savedLog = activityLogRepository.save(activityLog);
        log.info("Activity log saved: {} for user ID: {} - Success: {}", 
                savedLog.getId(), savedLog.getUserId(), savedLog.isSuccessful());
    }
    
    private void processUserCreationEvent(UserCreationEvent event) {
        try {
            // Log the user creation activity (no need to create user in DB since it's same database)
            ActivityLogDto activityLog = new ActivityLogDto();
            activityLog.setUserId(event.getUserId());
            activityLog.setIpAddress(event.getIpAddress());
            activityLog.setUserAgent(event.getUserAgent());
            activityLog.setSuccessful(true);
            
            LocalDateTime timestamp = LocalDateTime.parse(event.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            activityLog.setTimestamp(timestamp);
            activityLog.setFailureReason(null);
            activityLog.setAction("USER_CREATION");

            ActivityLogDto savedLog = activityLogRepository.save(activityLog);
            log.info("User creation activity log saved: {} for user ID: {} ({})", 
                    savedLog.getId(), savedLog.getUserId(), event.getUsername());
            
        } catch (Exception e) {
            log.error("Error processing user creation event for user {}: {}", event.getUsername(), e.getMessage(), e);
        }
    }
}
