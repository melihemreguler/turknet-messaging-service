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
import lombok.Getter;
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

        String threadId = createThreadId(senderId, recipientUserId);

        MessageCommand messageCommand = MessageCommand.create(
                threadId, senderId, senderUsername, recipient, content);

        kafkaProducerService.sendMessageCommand(messageCommand, senderId);

        log.info("Message command sent to Kafka from {} to {} in thread {}", senderId, recipient, threadId);

        return new MessageDto(threadId, senderId, senderUsername, content);
    }

    public PaginatedResponse<MessageDto> getConversationPaginated(HistoryRequest request) {
        String user1Id = resolveUserId(request.getUser1PrimaryId(), request.isUser1ByUserId());

        String user2Id = resolveUserId(request.getUser2PrimaryId(), request.isUser2ByUserId());

        String threadId = createThreadId(user1Id, user2Id);

        Pageable pageable = PageRequest.of(request.offset() / request.limit(), request.limit());

        Page<MessageDto> messagePage = messageRepository.findByThreadIdOrderByTimestampAsc(threadId, pageable);

        long total = messageRepository.countByThreadId(threadId);

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
        String user1Id = resolveUserId(request.getUser1PrimaryId(), request.isUser1ByUserId());
        String user2Id = resolveUserId(request.getUser2PrimaryId(), request.isUser2ByUserId());

        return new ConversationSecurityInfo(user1Id, user2Id);
    }

    private String resolveUserId(String identifier, boolean isUserId) {
        return isUserId ? resolveUserIdById(identifier) : resolveUserIdByUsername(identifier);
    }
    
    private String resolveUserIdById(String userId) {
        return userRepository.findById(userId)
            .map(UserDto::getId)
            .orElseThrow(() -> UserNotFoundException.forUserId(userId));
    }
    
    private String resolveUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(UserDto::getId)
            .orElseThrow(() -> UserNotFoundException.forUsername(username));
    }

    public record ConversationSecurityInfo(String user1Id, String user2Id) {

        public boolean isUserPartOfConversation(String currentUserId) {
            return currentUserId.equals(user1Id) || currentUserId.equals(user2Id);
        }

    }

    private String createThreadId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "-" + userId2;
        } else {
            return userId2 + "-" + userId1;
        }
    }
}
