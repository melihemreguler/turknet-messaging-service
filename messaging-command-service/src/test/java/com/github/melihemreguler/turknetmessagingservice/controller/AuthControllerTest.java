package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserResponseDto;
import com.github.melihemreguler.turknetmessagingservice.model.request.UserRegisterRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.LoginRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.service.UserService;
import com.github.melihemreguler.turknetmessagingservice.service.SessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Test
    void givenMultipleForwardedIps_whenRegisterUser_thenUsesFirstIp() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("testuser2", "test2@example.com", "password456");
        UserDto userDto = new UserDto();
        userDto.setId("user-id-2");
        userDto.setUsername("testuser2");
        userDto.setCreatedAt(java.time.LocalDateTime.now());
        String sessionId = "session-token-234";

        when(userService.registerUser(any(UserRegisterRequest.class), anyString(), anyString())).thenReturn(userDto);
        when(sessionService.createSession(eq("user-id-2"), eq("testuser2"), anyString(), anyString())).thenReturn(sessionId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"testuser2\"," +
                        "\"email\": \"test2@example.com\"," +
                        "\"password\": \"password456\"}")
                .header("User-Agent", "JUnitTestAgent")
                .header("X-Forwarded-For", "10.0.0.1, 192.168.1.1"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("X-Session-Id"))
                .andExpect(MockMvcResultMatchers.header().exists("X-User-Id"));
    }

    @Test
    void givenXRealIpHeader_whenRegisterUser_thenUsesXRealIp() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("testuser3", "test3@example.com", "password789");
        UserDto userDto = new UserDto();
        userDto.setId("user-id-3");
        userDto.setUsername("testuser3");
        userDto.setCreatedAt(java.time.LocalDateTime.now());
        String sessionId = "session-token-345";

        when(userService.registerUser(any(UserRegisterRequest.class), anyString(), anyString())).thenReturn(userDto);
        when(sessionService.createSession(eq("user-id-3"), eq("testuser3"), anyString(), anyString())).thenReturn(sessionId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"testuser3\"," +
                        "\"email\": \"test3@example.com\"," +
                        "\"password\": \"password789\"}")
                .header("User-Agent", "JUnitTestAgent")
                .header("X-Real-IP", "172.16.0.1"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("X-Session-Id"))
                .andExpect(MockMvcResultMatchers.header().exists("X-User-Id"));
    }

    @Test
    void givenNoIpHeaders_whenRegisterUser_thenUsesRemoteAddr() throws Exception {
        // Given
        UserRegisterRequest request = new UserRegisterRequest("testuser4", "test4@example.com", "password000");
        UserDto userDto = new UserDto();
        userDto.setId("user-id-4");
        userDto.setUsername("testuser4");
        userDto.setCreatedAt(java.time.LocalDateTime.now());
        String sessionId = "session-token-456";

        when(userService.registerUser(any(UserRegisterRequest.class), anyString(), anyString())).thenReturn(userDto);
        when(sessionService.createSession(eq("user-id-4"), eq("testuser4"), anyString(), anyString())).thenReturn(sessionId);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"testuser4\"," +
                        "\"email\": \"test4@example.com\"," +
                        "\"password\": \"password000\"}")
                .header("User-Agent", "JUnitTestAgent"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("X-Session-Id"))
                .andExpect(MockMvcResultMatchers.header().exists("X-User-Id"));
    }

    @Test
    void givenSessionIdIsEmpty_whenLogoutUser_thenReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
                .header("X-Session-Id", ""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No session Id provided"));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void givenValidRegisterRequest_whenRegisterUser_thenReturnsCreatedAndSessionHeaders() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest("testuser", "test@example.com", "password123");
        UserDto userDto = new UserDto();
        userDto.setId("user-id-1");
        userDto.setUsername("testuser");
        userDto.setCreatedAt(java.time.LocalDateTime.now());
        UserResponseDto responseDto = UserResponseDto.fromUserDto(userDto);
        String sessionId = "session-token-123";

        when(userService.registerUser(any(UserRegisterRequest.class), anyString(), anyString())).thenReturn(userDto);
        when(sessionService.createSession(eq("user-id-1"), eq("testuser"), anyString(), anyString())).thenReturn(sessionId);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"testuser\"," +
                        "\"email\": \"test@example.com\"," +
                        "\"password\": \"password123\"}")
                .header("User-Agent", "JUnitTestAgent")
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("X-Session-Id"))
                .andExpect(MockMvcResultMatchers.header().exists("X-User-Id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User registered successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void givenValidLoginRequest_whenLoginUser_thenReturnsOkAndSessionHeaders() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        UserService.AuthenticationResult authResult = new UserService.AuthenticationResult(true, null, "session-token-456", "user-id-2");

        when(userService.authenticateUser(any(LoginRequest.class), anyString(), anyString())).thenReturn(authResult);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"testuser\"," +
                        "\"password\": \"password123\"}")
                .header("User-Agent", "JUnitTestAgent")
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists("X-Session-Id"))
                .andExpect(MockMvcResultMatchers.header().exists("X-User-Id"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Login successful"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("user-id-2"));
    }

    @Test
    void givenInvalidLoginRequest_whenLoginUser_thenReturnsUnauthorized() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("wronguser", "wrongpass");
        UserService.AuthenticationResult authResult = new UserService.AuthenticationResult(false, "Invalid credentials", null, null);

        when(userService.authenticateUser(any(LoginRequest.class), anyString(), anyString())).thenReturn(authResult);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"username\": \"wronguser\"," +
                        "\"password\": \"wrongpass\"}")
                .header("User-Agent", "JUnitTestAgent")
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void givenValidSessionId_whenLogoutUser_thenReturnsOk() throws Exception {
        // given
        String sessionId = "session-token-789";

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
                .header("X-Session-Id", sessionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Logout successful"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("Session invalidated"));
    }

    @Test
    void givenNoSessionId_whenLogoutUser_thenReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("No session Id provided"));
    }
}
