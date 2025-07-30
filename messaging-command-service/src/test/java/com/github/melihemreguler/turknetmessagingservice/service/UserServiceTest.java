package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.ConflictException;
import com.github.melihemreguler.turknetmessagingservice.model.request.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.model.event.UserActivityEvent;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private SessionService sessionService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenNewUser_whenRegisterUser_thenUserIsSavedAndKafkaEventSent() {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("newuser", "pass", "mail@example.com");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpass");
        UserDto userDto = new UserDto();
        userDto.setUsername(request.username());
        userDto.setPasswordHash("hashedpass");
        userDto.setCreatedAt(LocalDateTime.now());
        userDto.setId("user-id");
        when(userRepository.save(any(UserDto.class))).thenReturn(userDto);

        // When
        UserDto result = userService.registerUser(request, "127.0.0.1", "agent");

        // Then
        assertEquals(request.username(), result.getUsername());
        verify(userRepository).save(any(UserDto.class));
        verify(kafkaProducerService).sendUserCommand(any(UserActivityEvent.class), eq("user-id"));
    }

    @Test
    void givenExistingUsername_whenRegisterUser_thenThrowsConflictException() {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("existing", "pass", "mail@example.com");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () ->
                userService.registerUser(request, "127.0.0.1", "agent"));
    }

    @Test
    void givenValidCredentials_whenAuthenticateUser_thenReturnsSuccess() {
        // Given
        LoginRequest request = new LoginRequest("user", "pass");
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setPasswordHash("hashedpass");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userDto));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When
        UserService.AuthenticationResult result = userService.authenticateUser(request, "127.0.0.1", "agent");

        // Then
        assertTrue(result.successful());
        assertNull(result.failureReason());
    }

    @Test
    void givenInvalidCredentials_whenAuthenticateUser_thenReturnsFailure() {
        // Given
        LoginRequest request = new LoginRequest("user", "wrongpass");
        UserDto userDto = new UserDto();
        userDto.setUsername("user");
        userDto.setPasswordHash("hashedpass");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userDto));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        UserService.AuthenticationResult result = userService.authenticateUser(request, "127.0.0.1", "agent");

        // Then
        assertFalse(result.successful());
        assertNotNull(result.failureReason());
    }

    @Test
    void givenNonexistentUser_whenAuthenticateUser_thenReturnsFailure() {
        // Given
        LoginRequest request = new LoginRequest("nouser", "pass");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When
        UserService.AuthenticationResult result = userService.authenticateUser(request, "127.0.0.1", "agent");

        // Then
        assertFalse(result.successful());
        assertNotNull(result.failureReason());
    }
}
