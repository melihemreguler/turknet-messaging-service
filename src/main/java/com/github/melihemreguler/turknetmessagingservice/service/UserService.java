package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.model.UserRegistrationRequest;
import com.github.melihemreguler.turknetmessagingservice.model.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    
    public UserDto registerUser(UserRegistrationRequest request) {
        String username = request.getTrimmedUsername();
        
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        Map<String, Object> userRegistrationCommand = new HashMap<>();
        userRegistrationCommand.put("command", "REGISTER_USER");
        userRegistrationCommand.put("username", username);
        userRegistrationCommand.put("password", request.password());
        userRegistrationCommand.put("timestamp", LocalDateTime.now());
        
        kafkaProducerService.sendUserCommand(userRegistrationCommand);
        
        log.info("User registration command sent to Kafka for username: {}", username);
        
        UserDto tempUser = new UserDto(username, "***PENDING***");
        return tempUser;
    }
    
    public boolean authenticateUser(LoginRequest request, String ipAddress, String userAgent) {
        String username = request.getTrimmedUsername();
        
        Optional<UserDto> userOpt = userRepository.findByUsername(username);
        boolean successful = false;
        String failureReason = null;
        
        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            if (passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                successful = true;
                log.info("User authenticated successfully: {}", username);
            } else {
                failureReason = "Invalid password";
                log.warn("Authentication failed for user {}: Invalid password", username);
            }
        } else {
            failureReason = "User not found";
            log.warn("Authentication failed for user {}: User not found", username);
        }
        
        // Send activity event to Kafka
        Map<String, Object> activityEvent = new HashMap<>();
        activityEvent.put("command", "LOG_USER_ACTIVITY");
        activityEvent.put("username", username);
        activityEvent.put("ipAddress", ipAddress);
        activityEvent.put("userAgent", userAgent);
        activityEvent.put("successful", successful);
        activityEvent.put("timestamp", LocalDateTime.now());
        if (failureReason != null) {
            activityEvent.put("failureReason", failureReason);
        }        kafkaProducerService.sendUserCommand(activityEvent);
        
        return successful;
    }
    
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username.trim().toLowerCase());
    }
}
