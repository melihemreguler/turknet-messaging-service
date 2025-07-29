package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import com.github.melihemreguler.turknetmessagingservice.model.request.ActivityLogsRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
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

    public PaginatedResponse<ActivityLogDto.ActivityEntry> getUserActivitiesPaginated(String userId, ActivityLogsRequest request) {
        Optional<ActivityLogDto> activityLogOpt = activityLogRepository.findByUserIdWithPagination(
                userId, request.offset(), request.limit());

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
