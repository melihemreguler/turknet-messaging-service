package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.model.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.model.UserActivityEvent;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final SessionService sessionService;
    
    public UserDto registerUser(UserRegisterRequest request, String ipAddress, String userAgent) {
        try {
            // Check if user already exists
            if (userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            
            UserDto userDto = new UserDto();
            userDto.setUsername(request.username());
            userDto.setPasswordHash(passwordEncoder.encode(request.password()));
            userDto.setCreatedAt(LocalDateTime.now());
            
            UserDto savedUser = userRepository.save(userDto);
            
            // Send user registration event to Kafka with IP and User-Agent
            UserActivityEvent registrationEvent = UserActivityEvent.createRegistration(
                savedUser.getUsername(), 
                savedUser.getId(),
                request.email(),
                savedUser.getPasswordHash(),
                ipAddress,
                userAgent
            );
            kafkaProducerService.sendUserCommand(registrationEvent);
            
            log.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
    }
    
    public AuthenticationResult authenticateUser(LoginRequest request, String ipAddress, String userAgent) {
        String username = request.getTrimmedUsername();
        
        Optional<UserDto> userOpt = userRepository.findByUsername(username);
        
        AuthenticationResult result = validateUserCredentials(userOpt, request.password(), username);
        
        // Create session if authentication successful
        String sessionToken = null;
        if (result.isSuccessful() && userOpt.isPresent()) {
            UserDto user = userOpt.get();
            sessionToken = sessionService.createSession(user.getId(), username, ipAddress, userAgent);
        }
        
        // Send activity event to Kafka
        String userId = userOpt.map(UserDto::getId).orElse(null);
        UserActivityEvent activityEvent = UserActivityEvent.create(
            username, userId, ipAddress, userAgent, result.isSuccessful(), result.getFailureReason());
        
        kafkaProducerService.sendUserCommand(activityEvent);
        
        return new AuthenticationResult(result.isSuccessful(), result.getFailureReason(), sessionToken);
    }
    
    private AuthenticationResult validateUserCredentials(Optional<UserDto> userOpt, String password, String username) {
        if (userOpt.isEmpty()) {
            String failureReason = "User not found";
            log.warn("Authentication failed for user {}: {}", username, failureReason);
            return new AuthenticationResult(false, failureReason, null);
        }
        
        UserDto user = userOpt.get();
        if (!verifyPassword(password, user.getPasswordHash())) {
            String failureReason = "Invalid password";
            log.warn("Authentication failed for user {}: {}", username, failureReason);
            return new AuthenticationResult(false, failureReason, null);
        }
        
        log.info("User authenticated successfully: {}", username);
        return new AuthenticationResult(true, null, null);
    }
    
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username.trim().toLowerCase());
    }
    
    // Inner class for authentication results
    public static class AuthenticationResult {
        private final boolean successful;
        private final String failureReason;
        private final String sessionToken;
        
        public AuthenticationResult(boolean successful, String failureReason, String sessionToken) {
            this.successful = successful;
            this.failureReason = failureReason;
            this.sessionToken = sessionToken;
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public String getFailureReason() {
            return failureReason;
        }
        
        public String getSessionToken() {
            return sessionToken;
        }
    }
}
