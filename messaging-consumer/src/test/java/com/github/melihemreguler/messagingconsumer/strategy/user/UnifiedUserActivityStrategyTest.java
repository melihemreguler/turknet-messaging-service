package com.github.melihemreguler.messagingconsumer.strategy.user;

import com.github.melihemreguler.messagingconsumer.dto.ActivityLogDto;
import com.github.melihemreguler.messagingconsumer.model.UserActivityEvent;
import com.github.melihemreguler.messagingconsumer.repository.ActivityLogRepository;
import com.github.melihemreguler.messagingconsumer.strategy.user.impl.UnifiedUserActivityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnifiedUserActivityStrategyTest {
    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private UnifiedUserActivityStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldLogUserCreation() {
        //GIVEN
        UserActivityEvent event = new UserActivityEvent();
        event.setUserId("user1");
        event.setCommand("USER_CREATION");
        event.setIpAddress("127.0.0.1");
        event.setUserAgent("JUnit");
        event.setSuccessful(true);
        event.setTimestamp(LocalDateTime.now());
        when(activityLogRepository.findByUserId("user1")).thenReturn(Optional.empty());
        when(activityLogRepository.save(any(ActivityLogDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        ArgumentCaptor<ActivityLogDto> captor = ArgumentCaptor.forClass(ActivityLogDto.class);
        verify(activityLogRepository).save(captor.capture());
        ActivityLogDto saved = captor.getValue();
        assertEquals("user1", saved.getUserId());
        assertEquals(1, saved.getLogs().size());
        ActivityLogDto.ActivityEntry entry = saved.getLogs().get(0);
        assertEquals("USER_CREATION", entry.getAction());
        assertTrue(entry.isSuccessful());
    }

    @Test
    void execute_shouldAppendLogForExistingUser() {
        //GIVEN
        UserActivityEvent event = new UserActivityEvent();
        event.setUserId("user2");
        event.setCommand("LOGIN_ATTEMPT");
        event.setIpAddress("127.0.0.2");
        event.setUserAgent("JUnit");
        event.setSuccessful(false);
        event.setFailureReason("Wrong password");
        event.setTimestamp(LocalDateTime.now());
        ActivityLogDto existing = new ActivityLogDto();
        existing.setUserId("user2");
        existing.getLogs().add(new ActivityLogDto.ActivityEntry("127.0.0.2", "JUnit", true, LocalDateTime.now(), null, "LOGIN_ATTEMPT"));
        when(activityLogRepository.findByUserId("user2")).thenReturn(Optional.of(existing));
        when(activityLogRepository.save(any(ActivityLogDto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //WHEN
        strategy.execute(event);

        //THEN
        ArgumentCaptor<ActivityLogDto> captor = ArgumentCaptor.forClass(ActivityLogDto.class);
        verify(activityLogRepository).save(captor.capture());
        ActivityLogDto saved = captor.getValue();
        assertEquals("user2", saved.getUserId());
        assertEquals(2, saved.getLogs().size());
        ActivityLogDto.ActivityEntry entry = saved.getLogs().get(1);
        assertEquals("LOGIN_ATTEMPT", entry.getAction());
        assertFalse(entry.isSuccessful());
        assertEquals("Wrong password", entry.getFailureReason());
    }

    @Test
    void execute_shouldHandleNullEvent() {
        //GIVEN
        UserActivityEvent event = null;

        //WHEN
        assertThrows(NullPointerException.class, () -> strategy.execute(event));

        //THEN
        // Exception expected
    }
}
