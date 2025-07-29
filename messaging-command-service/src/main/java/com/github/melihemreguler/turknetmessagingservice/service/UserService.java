package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.ConflictException;
import com.github.melihemreguler.turknetmessagingservice.model.request.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.model.event.UserActivityEvent;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import lombok.Getter;
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
        // Check if user already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }

        UserDto userDto = new UserDto();
        userDto.setUsername(request.username());
        userDto.setPasswordHash(passwordEncoder.encode(request.password()));
        userDto.setCreatedAt(LocalDateTime.now());

        UserDto savedUser = userRepository.save(userDto);


        UserActivityEvent activityEvent = UserActivityEvent.createUserCreation(
                savedUser.getUsername(),
                savedUser.getId(),
                request.email(),
                ipAddress,
                userAgent
        );
        kafkaProducerService.sendUserCommand(activityEvent, savedUser.getId());

        log.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public AuthenticationResult authenticateUser(LoginRequest request, String ipAddress, String userAgent) {
        String username = request.getTrimmedUsername();

        Optional<UserDto> userOpt = userRepository.findByUsername(username);

        AuthenticationResult result = validateUserCredentials(userOpt, request.password(), username);

        String sessionToken = null;
        if (result.successful() && userOpt.isPresent()) {
            UserDto user = userOpt.get();
            sessionToken = sessionService.createSession(user.getId(), username, ipAddress, userAgent);
        }

        String userId = userOpt.map(UserDto::getId).orElse(null);
        UserActivityEvent activityEvent = UserActivityEvent.createLoginAttempt(
                username, userId, ipAddress, userAgent, result.successful(), result.failureReason());

        kafkaProducerService.sendUserCommand(activityEvent, userId != null ? userId : "unknown");

        return new AuthenticationResult(result.successful(), result.failureReason(), sessionToken, userId);
    }

    private AuthenticationResult validateUserCredentials(Optional<UserDto> userOpt, String password, String username) {
        if (userOpt.isEmpty()) {
            String failureReason = "User not found";
            log.warn("Authentication failed for user {}: {}", username, failureReason);
            return new AuthenticationResult(false, failureReason, null, null);
        }

        UserDto user = userOpt.get();
        if (!verifyPassword(password, user.getPasswordHash())) {
            String failureReason = "Invalid password";
            log.warn("Authentication failed for user {}: {}", username, failureReason);
            return new AuthenticationResult(false, failureReason, null, user.getId());
        }

        log.info("User authenticated successfully: {}", username);
        return new AuthenticationResult(true, null, null, user.getId());
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public record AuthenticationResult(boolean successful, String failureReason, String sessionId, String userId) {

    }
}
