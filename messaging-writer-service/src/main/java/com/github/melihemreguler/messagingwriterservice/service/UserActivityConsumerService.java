package com.github.melihemreguler.messagingwriterservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.melihemreguler.messagingwriterservice.dto.ActivityLogDto;
import com.github.melihemreguler.messagingwriterservice.model.UserActivityEvent;
import com.github.melihemreguler.messagingwriterservice.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserActivityConsumerService {

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public UserActivityConsumerService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "${app.kafka.topics.user-commands}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserActivity(String message) {
        log.info("Received user activity: {}", message);
        
        try {
            UserActivityEvent event = objectMapper.readValue(message, UserActivityEvent.class);
            
            if ("LOG_USER_ACTIVITY".equals(event.getCommand())) {
                processUserActivityEvent(event);
            } else {
                log.warn("Unknown user activity command: {}", event.getCommand());
            }
        } catch (Exception e) {
            log.error("Error processing user activity: {}", e.getMessage(), e);
        }
    }

    private void processUserActivityEvent(UserActivityEvent event) {
        ActivityLogDto activityLog = new ActivityLogDto();
        activityLog.setUsername(event.getUsername());
        activityLog.setIpAddress(event.getIpAddress());
        activityLog.setUserAgent(event.getUserAgent());
        activityLog.setSuccessful(event.isSuccessful());
        activityLog.setTimestamp(event.getTimestamp());
        activityLog.setFailureReason(event.getFailureReason());
        activityLog.setAction("LOGIN_ATTEMPT");

        ActivityLogDto savedLog = activityLogRepository.save(activityLog);
        log.info("Activity log saved: {} for user {} - Success: {}", 
                savedLog.getId(), savedLog.getUsername(), savedLog.isSuccessful());
    }
}
