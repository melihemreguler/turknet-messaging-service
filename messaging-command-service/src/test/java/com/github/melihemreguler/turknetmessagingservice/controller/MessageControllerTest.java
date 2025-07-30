package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.model.request.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.MessageResponse;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {
    @Test
    void givenUserNotPartOfConversation_whenGetConversation_thenReturnsForbidden() throws Exception {
        when(messageService.getConversationSecurityInfo(any())).thenReturn(new MessageService.ConversationSecurityInfo("userA", "userB"));
        // userId "senderId" neither userA nor userB
        mockMvc.perform(MockMvcRequestBuilders.get("/api/messages/history")
                .param("userId", "recipientId")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Access denied to this conversation"));
    }

    @Test
    void givenNoMessagesFound_whenGetConversation_thenReturnsThreadNotFound() throws Exception {
        when(messageService.getConversationSecurityInfo(any())).thenReturn(new MessageService.ConversationSecurityInfo(USER_ID, "recipientId"));
        when(messageService.getConversationPaginated(any())).thenThrow(new com.github.melihemreguler.turknetmessagingservice.exception.ThreadNotFoundException("No conversation found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/messages/history")
                .param("userId", "recipientId")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    private static final String SESSION_ID = "test-session-id";
    private static final String USER_ID = "senderId";

    @org.junit.jupiter.api.BeforeEach
    void setupSessionMock() {
        com.github.melihemreguler.turknetmessagingservice.dto.SessionDto sessionDto = new com.github.melihemreguler.turknetmessagingservice.dto.SessionDto();
        sessionDto.setUserId(USER_ID);
        org.mockito.Mockito.when(sessionService.validateSession(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(java.util.Optional.of(sessionDto));
    }
    @MockitoBean
    private com.github.melihemreguler.turknetmessagingservice.service.SessionService sessionService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void givenValidSendMessageRequest_whenSendMessage_thenReturnsCreated() throws Exception {
        // given
        MessageRequest request = new MessageRequest("recipientUser", "Hello!");
        MessageDto messageDto = new MessageDto("thread-1", "senderId", "senderUser", "Hello!");
        MessageResponse response = new MessageResponse("thread-1", "senderId", "Hello!", LocalDateTime.now());

        when(messageService.sendMessage(anyString(), any(MessageRequest.class))).thenReturn(messageDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/messages/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipient\": \"recipientUser\", \"content\": \"Hello!\"}")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Message sent successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.threadId").value("thread-1"));
    }

    @Test
    void givenValidHistoryRequest_whenGetConversation_thenReturnsOk() throws Exception {
        // given
        MessageDto messageDto = new MessageDto("thread-1", "senderId", "senderUser", "Hello!");
        PaginatedResponse<MessageDto> paginated = PaginatedResponse.of(List.of(messageDto), 1, 50, 0);

        when(messageService.getConversationSecurityInfo(any())).thenReturn(new MessageService.ConversationSecurityInfo("senderId", "recipientId"));
        when(messageService.getConversationPaginated(any())).thenReturn(paginated);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/messages/history")
                .param("userId", "recipientId")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Conversation retrieved successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.data[0].threadId").value("thread-1"));
    }

    @Test
    void givenNoUserIdOrUsername_whenGetConversation_thenReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/messages/history")
                .param("limit", "50")
                .param("offset", "0")
                .header("X-Session-Id", SESSION_ID)
                .header("X-User-Id", USER_ID)
                .requestAttr("currentUserId", USER_ID))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Either userId or username parameter must be provided"));
    }
}
