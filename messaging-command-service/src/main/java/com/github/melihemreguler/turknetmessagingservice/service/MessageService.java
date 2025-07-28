package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.UserNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.exception.ThreadNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.model.api.HistoryRequest;
import com.github.melihemreguler.turknetmessagingservice.model.api.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.api.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.model.event.MessageCommand;
import com.github.melihemreguler.turknetmessagingservice.repository.MessageRepository;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    
    public MessageDto sendMessage(String senderId, MessageRequest request) {
        String recipient = request.getTrimmedRecipient();
        String content = request.getTrimmedContent();
        
        // Get sender user info
        Optional<UserDto> senderUser = userRepository.findById(senderId);
        if (senderUser.isEmpty()) {
            throw UserNotFoundException.forSender(senderId);
        }
        
        Optional<UserDto> recipientUser = userRepository.findByUsername(recipient);
        if (recipientUser.isEmpty()) {
            throw UserNotFoundException.forRecipient(recipient);
        }
        
        String senderUsername = senderUser.get().getUsername();
        String recipientUserId = recipientUser.get().getId();
        
        // Create thread ID (sorted userIds to ensure consistency)
        String threadId = createThreadId(senderId, recipientUserId);
        
        MessageCommand messageCommand = MessageCommand.create(
            threadId, senderId, senderUsername, recipient, content);
        
        kafkaProducerService.sendMessageCommand(messageCommand, senderId);
        
        log.info("Message command sent to Kafka from {} to {} in thread {}", senderId, recipient, threadId);
        
        MessageDto tempMessage = new MessageDto(threadId, senderId, senderUsername, content);
        
        return tempMessage;
    }
    
    public List<MessageDto> getConversation(String userId1, String userId2) {
        // Get usernames from userIds
        Optional<UserDto> user1Opt = userRepository.findById(userId1);
        Optional<UserDto> user2Opt = userRepository.findById(userId2);
        
        if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
            throw UserNotFoundException.forConversation();
        }
        
        String threadId = createThreadId(userId1, userId2);
        
        List<MessageDto> messages = messageRepository.findByThreadIdOrderByTimestampAsc(threadId);
        
        // If thread doesn't exist (no messages found), throw an exception
        if (messages.isEmpty()) {
            log.warn("No conversation found between users {} and {}", userId1, userId2);
            throw ThreadNotFoundException.forUsers(userId1, userId2);
        }
        
        return messages;
    }
    
    public List<MessageDto> getConversation(HistoryRequest request) {
        // Resolve user1 ID
        String user1Id = resolveUserId(request.getUser1PrimaryId(), request.isUser1ByUserId());
        
        // Resolve user2 ID  
        String user2Id = resolveUserId(request.getUser2PrimaryId(), request.isUser2ByUserId());
        
        // Delegate to existing method
        return getConversation(user1Id, user2Id);
    }
    
    public PaginatedResponse<MessageDto> getConversationPaginated(HistoryRequest request) {
        // Resolve user1 ID
        String user1Id = resolveUserId(request.getUser1PrimaryId(), request.isUser1ByUserId());
        
        // Resolve user2 ID  
        String user2Id = resolveUserId(request.getUser2PrimaryId(), request.isUser2ByUserId());
        
        String threadId = createThreadId(user1Id, user2Id);
        
        // Create pageable object
        Pageable pageable = PageRequest.of(request.offset() / request.limit(), request.limit());
        
        // Get paginated messages
        Page<MessageDto> messagePage = messageRepository.findByThreadIdOrderByTimestampAsc(threadId, pageable);
        
        // Get total count
        long total = messageRepository.countByThreadId(threadId);
        
        // If no messages found, log warning
        if (total == 0) {
            log.warn("No conversation found between users {} and {}", user1Id, user2Id);
            throw ThreadNotFoundException.forUsers(user1Id, user2Id);
        }
        
        return PaginatedResponse.of(
            messagePage.getContent(),
            total,
            request.limit(),
            request.offset()
        );
    }
    
    public ConversationSecurityInfo getConversationSecurityInfo(HistoryRequest request) {
        // Resolve user IDs for security check
        String user1Id = resolveUserId(request.getUser1PrimaryId(), request.isUser1ByUserId());
        String user2Id = resolveUserId(request.getUser2PrimaryId(), request.isUser2ByUserId());
        
        return new ConversationSecurityInfo(user1Id, user2Id);
    }
    
    private String resolveUserId(String identifier, boolean isUserId) {
        if (isUserId) {
            // Verify user exists by ID
            Optional<UserDto> userOpt = userRepository.findById(identifier);
            if (userOpt.isEmpty()) {
                throw UserNotFoundException.forUserId(identifier);
            }
            return identifier;
        } else {
            // Find user by username
            Optional<UserDto> userOpt = userRepository.findByUsername(identifier);
            if (userOpt.isEmpty()) {
                throw UserNotFoundException.forUsername(identifier);
            }
            return userOpt.get().getId();
        }
    }
    
    // Helper class for security info
    public static class ConversationSecurityInfo {
        private final String user1Id;
        private final String user2Id;
        
        public ConversationSecurityInfo(String user1Id, String user2Id) {
            this.user1Id = user1Id;
            this.user2Id = user2Id;
        }
        
        public boolean isUserPartOfConversation(String currentUserId) {
            return currentUserId.equals(user1Id) || currentUserId.equals(user2Id);
        }
        
        public String getUser1Id() { return user1Id; }
        public String getUser2Id() { return user2Id; }
    }
    
    private String createThreadId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "-" + userId2;
        } else {
            return userId2 + "-" + userId1;
        }
    }
}
