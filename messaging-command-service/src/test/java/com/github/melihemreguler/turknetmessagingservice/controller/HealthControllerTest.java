package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.model.response.ReadinessResponse;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.kafka.core.KafkaAdmin;

import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@WebMvcTest(HealthController.class)
public class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KafkaAdmin kafkaAdmin;

    @MockitoBean
    private com.github.melihemreguler.turknetmessagingservice.service.SessionService sessionService;

    @Test
    void healthEndpoint_shouldReturnUp() throws Exception {
        // Given
        // No setup required

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/health"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("UP"));
    }

    @Test
    void readinessEndpoint_shouldReturnUp_whenKafkaHealthy() throws Exception {
        // Given
        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            AdminClient adminClient = mock(AdminClient.class);
            when(AdminClient.create(anyMap())).thenReturn(adminClient);
            org.apache.kafka.clients.admin.ListTopicsResult listTopicsResult = mock(org.apache.kafka.clients.admin.ListTopicsResult.class);
            when(adminClient.listTopics(any(ListTopicsOptions.class))).thenReturn(listTopicsResult);
            org.apache.kafka.common.KafkaFuture<java.util.Set<String>> kafkaFuture = mock(org.apache.kafka.common.KafkaFuture.class);
            when(listTopicsResult.names()).thenReturn(kafkaFuture);
            when(kafkaFuture.get(anyLong(), any())).thenReturn(java.util.Set.of("topic1"));

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/ready"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"));
        }
    }

    @Test
    void readinessEndpoint_shouldReturn503_whenKafkaUnhealthy() throws Exception {
        // Given
        try (MockedStatic<AdminClient> adminClientMock = mockStatic(AdminClient.class)) {
            AdminClient adminClient = mock(AdminClient.class);
            when(AdminClient.create(anyMap())).thenReturn(adminClient);
            when(adminClient.listTopics(any(ListTopicsOptions.class))).thenThrow(new RuntimeException("Kafka down"));

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/ready"))
                    .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DOWN"));
        }
    }
}
