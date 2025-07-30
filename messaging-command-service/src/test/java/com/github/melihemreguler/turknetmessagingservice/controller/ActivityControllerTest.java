package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.ActivityLogDto;
import com.github.melihemreguler.turknetmessagingservice.model.request.ActivityLogsRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(ActivityController.class)
public class ActivityControllerTest {
    private static final String SESSION_ID = "test-session-id";
    private static final String USER_ID = "user-activity";

    @MockitoBean
    private com.github.melihemreguler.turknetmessagingservice.service.SessionService sessionService;

    @MockitoBean
    private ActivityService activityService;

    @org.junit.jupiter.api.BeforeEach
    void setupSessionMock() {
        com.github.melihemreguler.turknetmessagingservice.dto.SessionDto sessionDto = new com.github.melihemreguler.turknetmessagingservice.dto.SessionDto();
        sessionDto.setUserId(USER_ID);
        org.mockito.Mockito.when(sessionService.validateSession(anyString(), anyString()))
                .thenReturn(java.util.Optional.of(sessionDto));
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidRequest_whenGetUserActivityLogs_thenReturnsOk() throws Exception {
        // Given
        ActivityLogDto.ActivityEntry entry = new ActivityLogDto.ActivityEntry();
        PaginatedResponse<ActivityLogDto.ActivityEntry> paginated = PaginatedResponse.of(List.of(entry), 1, 50, 0);
        when(activityService.getUserActivitiesPaginated(eq(USER_ID), any(ActivityLogsRequest.class))).thenReturn(paginated);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/activities/logs")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Activity logs retrieved successfully"));
    }

    @Test
    void givenNoLogsFound_whenGetUserActivityLogs_thenReturnsEmptyList() throws Exception {
        // Given
        PaginatedResponse<ActivityLogDto.ActivityEntry> paginated = PaginatedResponse.of(List.of(), 0, 50, 0);
        when(activityService.getUserActivitiesPaginated(eq(USER_ID), any(ActivityLogsRequest.class))).thenReturn(paginated);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/activities/logs")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.data.length()", org.hamcrest.Matchers.is(0)));
    }
}
