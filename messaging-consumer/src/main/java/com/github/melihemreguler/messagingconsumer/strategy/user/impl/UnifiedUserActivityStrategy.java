package com.github.melihemreguler.messagingconsumer.strategy.user.impl;

import com.github.melihemreguler.messagingconsumer.dto.ActivityLogDto;
import com.github.melihemreguler.messagingconsumer.enums.UserActivityAction;
import com.github.melihemreguler.messagingconsumer.model.UserActivityEvent;
import com.github.melihemreguler.messagingconsumer.repository.ActivityLogRepository;
import com.github.melihemreguler.messagingconsumer.strategy.user.UserActivityStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnifiedUserActivityStrategy implements UserActivityStrategy {
    
    private final ActivityLogRepository activityLogRepository;
    
    @Override
    public void execute(Object event) {
        UserActivityEvent activityEvent = (UserActivityEvent) event;
        
        log.info("Processing user activity: {} for user: {} ({})", 
                activityEvent.getCommand(), activityEvent.getUserId(), activityEvent.getUsername());
        
        try {
            Optional<ActivityLogDto> existingLogOpt = activityLogRepository.findByUserId(activityEvent.getUserId());
            
            ActivityLogDto activityLog;
            if (existingLogOpt.isPresent()) {
                activityLog = existingLogOpt.get();
            } else {
                activityLog = new ActivityLogDto(activityEvent.getUserId());
            }
            
            // Determine the action based on activity type and success
            UserActivityAction action = determineAction(activityEvent);
            
            activityLog.addActivity(
                activityEvent.getIpAddress(),
                activityEvent.getUserAgent(),
                activityEvent.isSuccessful(),
                activityEvent.getTimestamp(),
                activityEvent.getFailureReason(),
                action.getValue()
            );

            ActivityLogDto savedLog = activityLogRepository.save(activityLog);
            log.info("User activity log updated: {} for user ID: {} - Action: {}", 
                    savedLog.getId(), savedLog.getUserId(), action);
            
        } catch (Exception e) {
            log.error("Error processing user activity for user {}: {}", 
                    activityEvent.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to process user activity", e);
        }
    }
    
    private UserActivityAction determineAction(UserActivityEvent event) {
        if (event.isUserCreation()) {
            return UserActivityAction.USER_CREATION;
        } else if (event.isLoginAttempt()) {
            return UserActivityAction.fromLoginResult(event.isSuccessful());
        } else {
            throw new IllegalArgumentException("Unknown activity type: " + event.getCommand());
        }
    }
}
