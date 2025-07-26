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
    
    public UserDto registerUser(UserRegisterRequest request) {
        try {
            UserDto userDto = new UserDto();
            userDto.setUsername(request.username());
            userDto.setPasswordHash(passwordEncoder.encode(request.password()));
            userDto.setCreatedAt(LocalDateTime.now());
            
            UserDto savedUser = userRepository.save(userDto);
            log.info("User registered successfully: {}", savedUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
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
        UserActivityEvent activityEvent = UserActivityEvent.create(
            username, ipAddress, userAgent, successful, failureReason);
        
        kafkaProducerService.sendUserCommand(activityEvent);
        
        return successful;
    }
    
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username.trim().toLowerCase());
    }
}
