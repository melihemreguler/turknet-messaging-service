package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.UserNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.exception.ThreadNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.model.request.HistoryRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.ConversationResponse;
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
        when(messageRepository.findByThreadIdOrderByTimestampDesc(eq(threadId), any())).thenReturn(page);
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
        when(messageRepository.findByThreadIdOrderByTimestampDesc(eq(threadId), any())).thenReturn(emptyPage);
        when(messageRepository.countByThreadId(threadId)).thenReturn(0L);

        // When & Then
        assertThrows(ThreadNotFoundException.class, () -> messageService.getConversationPaginated(request));
    }

    @Test
    void givenUserWithNoThreads_whenGetInbox_thenReturnsEmptyPage() {
        // Given
        String userId = "userA";
        when(messageRepository.countThreadsForUser(userId)).thenReturn(0L);

        // When
        PaginatedResponse<ConversationResponse> result = messageService.getInbox(userId, 20, 0);

        // Then
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotal());
        verify(messageRepository, never()).findLatestPerThreadForUser(any(), anyInt(), anyInt());
    }

    @Test
    void givenUserWithThreads_whenGetInbox_thenResolvesOtherUsernameAndReturnsConversations() {
        // Given
        String userId = "userA";
        // Two threads: userA-userB (other = userB) and userA-userC (other = userC)
        MessageDto m1 = new MessageDto("userA-userB", "userB", "userB", "hi A");
        MessageDto m2 = new MessageDto("userA-userC", "userA", "userA", "later");
        when(messageRepository.countThreadsForUser(userId)).thenReturn(2L);
        when(messageRepository.findLatestPerThreadForUser(userId, 20, 0))
                .thenReturn(List.of(m1, m2));

        UserDto b = new UserDto(); b.setId("userB"); b.setUsername("bob");
        UserDto c = new UserDto(); c.setId("userC"); c.setUsername("carol");
        when(userRepository.findAllById(argThat((Iterable<String> ids) -> {
            java.util.Set<String> s = new java.util.HashSet<>();
            ids.forEach(s::add);
            return s.equals(java.util.Set.of("userB", "userC"));
        }))).thenReturn(List.of(b, c));

        // When
        PaginatedResponse<ConversationResponse> result = messageService.getInbox(userId, 20, 0);

        // Then
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getData().size());
        ConversationResponse first = result.getData().get(0);
        assertEquals("userA-userB", first.getThreadId());
        assertEquals("userB", first.getOtherUserId());
        assertEquals("bob", first.getOtherUsername());
        assertEquals(m1, first.getLastMessage());

        ConversationResponse second = result.getData().get(1);
        assertEquals("userC", second.getOtherUserId());
        assertEquals("carol", second.getOtherUsername());
    }

    @Test
    void givenMissingUserLookup_whenGetInbox_thenFallsBackToUnknownUsername() {
        // Given
        String userId = "userA";
        MessageDto m = new MessageDto("userA-ghostX", "userA", "userA", "hello?");
        when(messageRepository.countThreadsForUser(userId)).thenReturn(1L);
        when(messageRepository.findLatestPerThreadForUser(userId, 20, 0)).thenReturn(List.of(m));
        when(userRepository.findAllById(any(Iterable.class))).thenReturn(List.of());

        // When
        PaginatedResponse<ConversationResponse> result = messageService.getInbox(userId, 20, 0);

        // Then
        assertEquals(1, result.getData().size());
        ConversationResponse conv = result.getData().get(0);
        assertEquals("ghostX", conv.getOtherUserId());
        assertEquals("unknown", conv.getOtherUsername());
    }
}
