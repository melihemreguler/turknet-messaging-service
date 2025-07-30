package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import com.github.melihemreguler.turknetmessagingservice.model.request.ActivityLogsRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ActivityServiceTest {
    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenLogsExist_whenGetUserActivitiesPaginated_thenReturnsPaginatedResponse() {
        // Given
        String userId = "user-1";
        ActivityLogsRequest request = new ActivityLogsRequest(0, 10);
        ActivityLogDto.ActivityEntry entry = new ActivityLogDto.ActivityEntry();
        ActivityLogDto activityLogDto = new ActivityLogDto();
        activityLogDto.setLogs(List.of(entry));
        when(activityLogRepository.findByUserIdWithPagination(eq(userId), anyInt(), anyInt())).thenReturn(Optional.of(activityLogDto));
        when(activityLogRepository.countLogsByUserId(userId)).thenReturn(Optional.of(1));

        // When
        PaginatedResponse<ActivityLogDto.ActivityEntry> result = activityService.getUserActivitiesPaginated(userId, request);

        // Then
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    void givenNoLogsExist_whenGetUserActivitiesPaginated_thenReturnsEmptyPaginatedResponse() {
        // Given
        String userId = "user-1";
        ActivityLogsRequest request = new ActivityLogsRequest(0, 10);
        when(activityLogRepository.findByUserIdWithPagination(eq(userId), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(activityLogRepository.countLogsByUserId(userId)).thenReturn(Optional.of(0));

        // When
        PaginatedResponse<ActivityLogDto.ActivityEntry> result = activityService.getUserActivitiesPaginated(userId, request);

        // Then
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotal());
    }
}
