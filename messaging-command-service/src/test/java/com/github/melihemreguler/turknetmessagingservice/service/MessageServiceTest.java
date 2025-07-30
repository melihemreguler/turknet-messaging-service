package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.UserNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.exception.ThreadNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.model.request.HistoryRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.model.event.MessageCommand;
import com.github.melihemreguler.turknetmessagingservice.repository.MessageRepository;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenValidSenderAndRecipient_whenSendMessage_thenReturnsMessageDto() {
        // Given
        String senderId = "sender-id";
        MessageRequest request = new MessageRequest("recipient", "content");
        UserDto senderUser = new UserDto(); senderUser.setId(senderId); senderUser.setUsername("sender");
        UserDto recipientUser = new UserDto(); recipientUser.setId("recipient-id"); recipientUser.setUsername("recipient");
        when(userRepository.findById(senderId)).thenReturn(Optional.of(senderUser));
        when(userRepository.findByUsername("recipient")).thenReturn(Optional.of(recipientUser));

        // When
        MessageDto result = messageService.sendMessage(senderId, request);

        // Then
        assertEquals(senderId, result.getSenderId());
        assertEquals("sender", result.getSenderUsername());
        assertEquals("content", result.getContent());
        verify(kafkaProducerService).sendMessageCommand(any(MessageCommand.class), eq(senderId));
    }

    @Test
    void givenInvalidSender_whenSendMessage_thenThrowsUserNotFoundException() {
        // Given
        String senderId = "invalid-id";
        MessageRequest request = new MessageRequest("recipient", "content");
        when(userRepository.findById(senderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> messageService.sendMessage(senderId, request));
    }

    @Test
    void givenInvalidRecipient_whenSendMessage_thenThrowsUserNotFoundException() {
        // Given
        String senderId = "sender-id";
        MessageRequest request = new MessageRequest("recipient", "content");
        UserDto senderUser = new UserDto(); senderUser.setId(senderId); senderUser.setUsername("sender");
        when(userRepository.findById(senderId)).thenReturn(Optional.of(senderUser));
        when(userRepository.findByUsername("recipient")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> messageService.sendMessage(senderId, request));
    }

    @Test
    void givenValidThreadId_whenGetConversation_thenReturnsPaginatedResponse() {
        // Given
        // threadId must match createThreadId logic: user1-user2
        String threadId = "user1-user2";
        HistoryRequest request = new HistoryRequest("user1", "user2", null, 10, 0);
        UserDto user1 = new UserDto(); user1.setId("user1"); user1.setUsername("user1");
        UserDto user2 = new UserDto(); user2.setId("user2"); user2.setUsername("user2");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findById("user2")).thenReturn(Optional.of(user2));
        MessageDto message = new MessageDto(threadId, "sender-id", "sender", "content");
        List<MessageDto> messages = List.of(message);
        org.springframework.data.domain.Page<MessageDto> page = new org.springframework.data.domain.PageImpl<>(messages);
        when(messageRepository.findByThreadIdOrderByTimestampAsc(eq(threadId), any())).thenReturn(page);
        when(messageRepository.countByThreadId(threadId)).thenReturn((long) messages.size());

        // When
        PaginatedResponse<MessageDto> result = messageService.getConversationPaginated(request);

        // Then
        assertEquals(1, result.getData().size());
        assertEquals("content", result.getData().get(0).getContent());
    }

    @Test
    void givenInvalidThreadId_whenGetConversation_thenThrowsThreadNotFoundException() {
        // Given
        String threadId = "invalid-thread";
        HistoryRequest request = new HistoryRequest("user1", "user2", null, 10, 0);
        UserDto user1 = new UserDto(); user1.setId("user1"); user1.setUsername("user1");
        UserDto user2 = new UserDto(); user2.setId("user2"); user2.setUsername("user2");
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findById("user2")).thenReturn(Optional.of(user2));
        org.springframework.data.domain.Page<MessageDto> emptyPage = new org.springframework.data.domain.PageImpl<>(List.of());
        when(messageRepository.findByThreadIdOrderByTimestampAsc(eq(threadId), any())).thenReturn(emptyPage);
        when(messageRepository.countByThreadId(threadId)).thenReturn(0L);

        // When & Then
        assertThrows(ThreadNotFoundException.class, () -> messageService.getConversationPaginated(request));
    }
}
