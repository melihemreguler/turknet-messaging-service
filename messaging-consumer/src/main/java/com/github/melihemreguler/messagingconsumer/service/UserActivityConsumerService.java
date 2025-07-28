package com.github.melihemreguler.messagingconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingconsumer.dto.ActivityLogDto;
import com.github.melihemreguler.messagingconsumer.dto.UserDto;
import com.github.melihemreguler.messagingconsumer.enums.ActivityType;
import com.github.melihemreguler.messagingconsumer.enums.CommandType;
import com.github.melihemreguler.messagingconsumer.model.UserActivityEvent;
import com.github.melihemreguler.messagingconsumer.model.UserCreationEvent;
import com.github.melihemreguler.messagingconsumer.repository.ActivityLogRepository;
import com.github.melihemreguler.messagingconsumer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
            if (message.contains(CommandType.CREATE_USER.getValue())) {
                UserCreationEvent creationEvent = objectMapper.readValue(message, UserCreationEvent.class);
                processUserCreationEvent(creationEvent);
                return;
            }
            
            // Parse as UserActivityEvent for other commands
            UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
            
            switch (event.getCommand()) {
                case "LOG_USER_ACTIVITY" -> {
                    if (CommandType.LOG_USER_ACTIVITY.getValue().equals(event.getCommand())) {
                        processUserActivityEvent(event);
                    }
                }
                default -> {
                    log.warn("Unknown user activity command: {}", event.getCommand());
                    // Try to parse with enum for validation
                    try {
                        CommandType.fromString(event.getCommand());
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
        Optional<ActivityLogDto> existingLogOpt = activityLogRepository.findByUserId(event.getUserId());
        
        ActivityLogDto activityLog;
        if (existingLogOpt.isPresent()) {
            activityLog = existingLogOpt.get();
        } else {
            activityLog = new ActivityLogDto(event.getUserId());
        }
        
        activityLog.addActivity(
            event.getIpAddress(),
            event.getUserAgent(),
            event.isSuccessful(),
            event.getTimestamp(),
            event.getFailureReason(),
            ActivityType.LOGIN_ATTEMPT.getValue()
        );

        ActivityLogDto savedLog = activityLogRepository.save(activityLog);
        log.info("Activity log updated: {} for user ID: {} - Success: {}", 
                savedLog.getId(), savedLog.getUserId(), event.isSuccessful());
    }
    
    private void processUserCreationEvent(UserCreationEvent event) {
        try {
            Optional<ActivityLogDto> existingLogOpt = activityLogRepository.findByUserId(event.getUserId());
            
            ActivityLogDto activityLog;
            if (existingLogOpt.isPresent()) {
                activityLog = existingLogOpt.get();
            } else {
                activityLog = new ActivityLogDto(event.getUserId());
            }
            
            LocalDateTime timestamp = LocalDateTime.parse(event.getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            activityLog.addActivity(
                event.getIpAddress(),
                event.getUserAgent(),
                true,
                timestamp,
                null,
                ActivityType.USER_CREATION.getValue()
            );

            ActivityLogDto savedLog = activityLogRepository.save(activityLog);
            log.info("User creation activity log updated: {} for user ID: {} ({})", 
                    savedLog.getId(), savedLog.getUserId(), event.getUsername());
            
        } catch (Exception e) {
            log.error("Error processing user creation event for user {}: {}", event.getUsername(), e.getMessage(), e);
        }
    }
}
