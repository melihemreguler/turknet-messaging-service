package com.github.melihemreguler.turknetmessagingservice.interceptor;

import com.github.melihemreguler.turknetmessagingservice.dto.SessionDto;
import com.github.melihemreguler.turknetmessagingservice.enums.SessionConstants;
import com.github.melihemreguler.turknetmessagingservice.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionInterceptor implements HandlerInterceptor {
    
    private final SessionService sessionService;

    // Public endpoints that don't require authentication
    private static final Set<String> PUBLIC_ENDPOINT_PREFIXES = Set.of(
        "/api/auth/",
        "/api/health",
        "/api/ping", 
        "/api/ready",
        "/actuator/",
        "/v3/api-docs",
        "/api/swagger-ui"
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestURI)) {
            return true;
        }
        
        String sessionId = request.getHeader(SessionConstants.SESSION_ID_HEADER);
        String userId = request.getHeader(SessionConstants.USER_ID_HEADER);
        
        // Validate session ID
        if (!StringUtils.hasText(sessionId)) {
            return handleAuthenticationError(response, "Session ID required", requestURI);
        }
        
        // Validate user ID
        if (!StringUtils.hasText(userId)) {
            return handleAuthenticationError(response, "User ID required", requestURI);
        }
        
        // Validate session
        Optional<SessionDto> sessionOpt = sessionService.validateSession(sessionId, userId);
        if (sessionOpt.isEmpty()) {
            return handleAuthenticationError(response, "Invalid or expired session", requestURI);
        }
        
        // Set user context and response headers
        SessionDto session = sessionOpt.get();
        setUserContext(request, session);
        setResponseHeaders(response, sessionId, userId);
        
        log.debug("Session validated for user ID: {} on endpoint: {}", session.getUserId(), requestURI);
        return true;
    }
    
    /**
     * Handle authentication errors with consistent response format
     */
    private boolean handleAuthenticationError(HttpServletResponse response, String errorMessage, String requestURI) throws IOException {
        log.warn("{} for protected endpoint: {}", errorMessage, requestURI);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"%s\"}", errorMessage));
        return false;
    }
    
    /**
     * Set user context in request attributes
     */
    private void setUserContext(HttpServletRequest request, SessionDto session) {
        request.setAttribute(SessionConstants.USER_ID_ATTRIBUTE, session.getUserId());
    }
    
    /**
     * Set session and user ID in response headers for client use
     */
    private void setResponseHeaders(HttpServletResponse response, String sessionId, String userId) {
        response.addHeader(SessionConstants.SESSION_ID_HEADER, sessionId);
        response.addHeader(SessionConstants.USER_ID_HEADER, userId);
    }
    
    /**
     * Check if the given URI corresponds to a public endpoint
     */
    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINT_PREFIXES.stream()
                .anyMatch(requestURI::startsWith);
    }
}
