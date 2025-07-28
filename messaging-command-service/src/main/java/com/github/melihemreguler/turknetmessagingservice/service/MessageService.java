package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
import com.github.melihemreguler.turknetmessagingservice.exception.UserNotFoundException;
import com.github.melihemreguler.turknetmessagingservice.model.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.MessageCommand;
import com.github.melihemreguler.turknetmessagingservice.repository.MessageRepository;
import com.github.melihemreguler.turknetmessagingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.UUID;

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
        
        // Create thread ID (sorted usernames to ensure consistency)
        String threadId = createThreadId(senderUsername, recipient);
        
        // Generate a temporary message ID for immediate response
        String tempMessageId = UUID.randomUUID().toString();
        
        MessageCommand messageCommand = MessageCommand.create(
            tempMessageId, threadId, senderUsername, recipient, content);
        
        kafkaProducerService.sendMessageCommand(messageCommand, senderId);
        
        log.info("Message command sent to Kafka from {} to {} in thread {}", senderUsername, recipient, threadId);
        

        MessageDto tempMessage = new MessageDto(threadId, senderUsername, content);
        tempMessage.setId(tempMessageId);
        
        return tempMessage;
    }
    
    public List<MessageDto> getConversation(String userId1, String userId2) {
        // Get usernames from userIds
        Optional<UserDto> user1Opt = userRepository.findById(userId1);
        Optional<UserDto> user2Opt = userRepository.findById(userId2);
        
        if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
            throw UserNotFoundException.forConversation();
        }
        
        String username1 = user1Opt.get().getUsername().trim().toLowerCase();
        String username2 = user2Opt.get().getUsername().trim().toLowerCase();
        String threadId = createThreadId(username1, username2);
        
        return messageRepository.findByThreadIdOrderByTimestampAsc(threadId);
    }
    
    public List<MessageDto> getUserMessages(String userId) {
        // Get username from userId
        Optional<UserDto> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw UserNotFoundException.forUserId(userId);
        }
        
        String username = userOpt.get().getUsername().trim().toLowerCase();
        return messageRepository.findBySenderOrderByTimestampDesc(username);
    }
    
    private String createThreadId(String username1, String username2) {
        List<String> users = Arrays.asList(username1, username2);
        Collections.sort(users);
        return String.join("-", users);
    }
}
