package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.enums.SessionConstants;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserResponseDto;
import com.github.melihemreguler.turknetmessagingservice.model.response.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.request.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.LoginRequest;
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
    

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody @Valid UserRegisterRequest request,
                                                                    HttpServletRequest httpRequest) {
        log.info("User registration request received for username: {}", request.username());
        
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader(SessionConstants.USER_AGENT_HEADER);
        
        UserDto user = userService.registerUser(request, ipAddress, userAgent);
        
        String sessionId = sessionService.createSession(user.getId(), user.getUsername(), ipAddress, userAgent);
        
        UserResponseDto responseUser = UserResponseDto.fromUserDto(user);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(SessionConstants.SESSION_ID_HEADER, sessionId);
        headers.add(SessionConstants.USER_ID_HEADER, user.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .body(ApiResponse.success("User registered successfully", responseUser));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginUser(@RequestBody @Valid LoginRequest request,
                                                        HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader(SessionConstants.USER_AGENT_HEADER);
        
        log.info("Login attempt for username: {} from IP: {}", request.username(), ipAddress);
        
        UserService.AuthenticationResult result = userService.authenticateUser(request, ipAddress, userAgent);
        
        if (result.successful()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(SessionConstants.SESSION_ID_HEADER, result.sessionId());
            headers.add(SessionConstants.USER_ID_HEADER, result.userId());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(ApiResponse.success("Login successful", result.userId()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(@RequestHeader(value = SessionConstants.SESSION_ID_HEADER, required = false) String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            sessionService.invalidateSession(sessionId);
            log.info("User logged out successfully");
            return ResponseEntity.ok(ApiResponse.success("Logout successful", "Session invalidated"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No session Id provided"));
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(SessionConstants.X_FORWARDED_FOR_HEADER);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader(SessionConstants.X_REAL_IP_HEADER);
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
