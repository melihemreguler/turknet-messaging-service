package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import com.github.melihemreguler.turknetmessagingservice.enums.SessionConstants;
import com.github.melihemreguler.turknetmessagingservice.model.api.ActivityLogsRequest;
import com.github.melihemreguler.turknetmessagingservice.model.api.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.api.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {
    
    private final ActivityService activityService;

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PaginatedResponse<ActivityLogDto.ActivityEntry>>> getUserActivityLogs(
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            HttpServletRequest httpRequest) {
        
        String currentUserId = (String) httpRequest.getAttribute(SessionConstants.USER_ID_ATTRIBUTE.toString());
        
        // Create ActivityLogsRequest from query parameters
        ActivityLogsRequest request = new ActivityLogsRequest(limit, offset);
        
        log.info("Fetching activity logs for user ID: {} (limit: {}, offset: {})", currentUserId, request.limit(), request.offset());
        PaginatedResponse<ActivityLogDto.ActivityEntry> paginatedLogs = activityService.getUserActivitiesPaginated(currentUserId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Activity logs retrieved successfully", paginatedLogs));
    }
}
