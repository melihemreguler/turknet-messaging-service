package com.github.melihemreguler.turknetmessagingservice.interceptor;

import com.github.melihemreguler.turknetmessagingservice.dto.SessionDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.service.SessionService;
import com.github.melihemreguler.turknetmessagingservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionTokenInterceptor implements HandlerInterceptor {
    
    private final SessionService sessionService;
    private final UserService userService;
    private static final String SESSION_TOKEN_HEADER = "X-Session-Token";
    private static final String USER_ID_ATTRIBUTE = "currentUserId";
    private static final String USERNAME_ATTRIBUTE = "currentUser";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Skip authentication for auth endpoints and health checks
        if (isPublicEndpoint(requestURI)) {
            return true;
        }
        
        String sessionToken = request.getHeader(SESSION_TOKEN_HEADER);
        
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            log.warn("No session token provided for protected endpoint: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Session token required\"}");
            response.setContentType("application/json");
            return false;
        }
        
        Optional<SessionDto> sessionOpt = sessionService.validateSession(sessionToken);
        
        if (sessionOpt.isEmpty()) {
            log.warn("Invalid or expired session token for endpoint: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired session\"}");
            response.setContentType("application/json");
            return false;
        }
        
        SessionDto session = sessionOpt.get();
        request.setAttribute(USER_ID_ATTRIBUTE, session.getUserId());
        request.setAttribute(USERNAME_ATTRIBUTE, session.getUserId()); // Use userId as currentUser
        
        // Add session token back to response header
        response.addHeader(SESSION_TOKEN_HEADER, sessionToken);
        
        log.debug("Session validated for user ID: {} on endpoint: {}", session.getUserId(), requestURI);
        return true;
    }
    
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/health") ||
               requestURI.startsWith("/api/ping") ||
               requestURI.startsWith("/api/ready") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/api/swagger-ui");
    }
}
