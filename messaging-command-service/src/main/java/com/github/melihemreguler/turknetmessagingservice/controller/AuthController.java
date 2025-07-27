package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.model.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.service.UserService;
import com.github.melihemreguler.turknetmessagingservice.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    private final SessionService sessionService;
    
    private static final String SESSION_TOKEN_HEADER = "X-Session-Token";
    private static final String USER_ID_HEADER = "X-User-Id";
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestBody @Valid UserRegisterRequest request,
                                                            HttpServletRequest httpRequest) {
        try {
            log.info("User registration request received for username: {}", request.username());
            
            // Get client info
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            UserDto user = userService.registerUser(request, ipAddress, userAgent);
            
            // Create session for the newly registered user
            String sessionToken = sessionService.createSession(user.getId(), user.getUsername(), ipAddress, userAgent);
            
            UserDto responseUser = new UserDto();
            responseUser.setId(user.getId());
            responseUser.setUsername(user.getUsername());
            responseUser.setCreatedAt(user.getCreatedAt());
            responseUser.setPasswordHash(null);
            
            // Add session token and userId to response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(SESSION_TOKEN_HEADER, sessionToken);
            headers.add(USER_ID_HEADER, user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(ApiResponse.success("User registered successfully", responseUser));
        } catch (IllegalArgumentException e) {
            log.warn("User registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginUser(@RequestBody @Valid LoginRequest request,
                                                        HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            log.info("Login attempt for username: {} from IP: {}", request.username(), ipAddress);
            
            UserService.AuthenticationResult result = userService.authenticateUser(request, ipAddress, userAgent);
            
            if (result.isSuccessful()) {
                // Add session token and userId to response headers
                HttpHeaders headers = new HttpHeaders();
                headers.add(SESSION_TOKEN_HEADER, result.getSessionToken());
                headers.add(USER_ID_HEADER, result.getUserId());
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(ApiResponse.success("Login successful", "Authentication successful"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid credentials"));
            }
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(@RequestHeader(value = SESSION_TOKEN_HEADER, required = false) String sessionToken) {
        try {
            if (sessionToken != null && !sessionToken.trim().isEmpty()) {
                sessionService.invalidateSession(sessionToken);
                log.info("User logged out successfully");
                return ResponseEntity.ok(ApiResponse.success("Logout successful", "Session invalidated"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("No session token provided"));
            }
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
