package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import com.github.melihemreguler.turknetmessagingservice.model.api.ActivityLogsRequest;
import com.github.melihemreguler.turknetmessagingservice.model.api.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    
    private final ActivityLogRepository activityLogRepository;
    
    public ActivityLogDto getUserActivities(String userId) {
        Optional<ActivityLogDto> activityLogOpt = activityLogRepository.findByUserId(userId);
        
        if (activityLogOpt.isEmpty()) {
            log.info("No activity logs found for user: {}", userId);
            // Return empty activity log instead of null
            return new ActivityLogDto(userId);
        }
        
        return activityLogOpt.get();
    }
    
    public PaginatedResponse<ActivityLogDto.ActivityEntry> getUserActivitiesPaginated(String userId, ActivityLogsRequest request) {
        // Get paginated activity logs using MongoDB slice
        Optional<ActivityLogDto> activityLogOpt = activityLogRepository.findByUserIdWithPagination(
            userId, request.offset(), request.limit());
        
        // Get total count of logs
        Optional<Integer> totalCountOpt = activityLogRepository.countLogsByUserId(userId);
        long total = totalCountOpt.orElse(0);
        
        if (activityLogOpt.isEmpty() || activityLogOpt.get().getLogs().isEmpty()) {
            log.info("No activity logs found for user: {} (offset: {}, limit: {})", userId, request.offset(), request.limit());
            return PaginatedResponse.of(
                java.util.List.of(),
                total,
                request.limit(),
                request.offset()
            );
        }
        
        return PaginatedResponse.of(
            activityLogOpt.get().getLogs(),
            total,
            request.limit(),
            request.offset()
        );
    }
}
