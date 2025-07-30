package com.github.melihemreguler.turknetmessagingservice.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import com.github.melihemreguler.turknetmessagingservice.service.SessionService;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

class SessionInterceptorTest {
    @Test
    void givenPublicEndpoint_whenPreHandle_thenReturnsTrue() throws Exception {
        // Given
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        HandlerInterceptor interceptor = new SessionInterceptor(mockSessionService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        // When
        boolean result = interceptor.preHandle(request, response, handler);
        // Then
        assertTrue(result);
    }

    @Test
    void givenValidSession_whenPreHandle_thenReturnsTrue() throws Exception {
        // Given
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        HandlerInterceptor interceptor = new SessionInterceptor(mockSessionService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected/resource");
        request.addHeader("X-Session-Id", "session-id");
        request.addHeader("X-User-Id", "user-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        com.github.melihemreguler.turknetmessagingservice.dto.SessionDto sessionDto = Mockito.mock(com.github.melihemreguler.turknetmessagingservice.dto.SessionDto.class);
        Mockito.when(mockSessionService.validateSession("session-id", "user-id")).thenReturn(java.util.Optional.of(sessionDto));
        // When
        boolean result = interceptor.preHandle(request, response, handler);
        // Then
        assertTrue(result);
    }

    @Test
    void givenMissingSessionId_whenPreHandle_thenReturnsFalseAnd401() throws Exception {
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        HandlerInterceptor interceptor = new SessionInterceptor(mockSessionService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected/resource");
        request.addHeader("X-User-Id", "user-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        boolean result = interceptor.preHandle(request, response, handler);
        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Session ID required"));
    }

    @Test
    void givenMissingUserId_whenPreHandle_thenReturnsFalseAnd401() throws Exception {
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        HandlerInterceptor interceptor = new SessionInterceptor(mockSessionService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected/resource");
        request.addHeader("X-Session-Id", "session-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        boolean result = interceptor.preHandle(request, response, handler);
        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("User ID required"));
    }

    @Test
    void givenInvalidSession_whenPreHandle_thenReturnsFalseAnd401() throws Exception {
        SessionService mockSessionService = Mockito.mock(SessionService.class);
        HandlerInterceptor interceptor = new SessionInterceptor(mockSessionService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected/resource");
        request.addHeader("X-Session-Id", "session-id");
        request.addHeader("X-User-Id", "user-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();
        Mockito.when(mockSessionService.validateSession("session-id", "user-id")).thenReturn(java.util.Optional.empty());
        boolean result = interceptor.preHandle(request, response, handler);
        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or expired session"));
    }
}
