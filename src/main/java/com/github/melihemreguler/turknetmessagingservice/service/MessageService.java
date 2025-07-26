package com.github.melihemreguler.turknetmessagingservice.service;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.dto.UserDto;
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
    
    public MessageDto sendMessage(String senderUsername, MessageRequest request) {
        String sender = senderUsername.trim().toLowerCase();
        String recipient = request.getTrimmedRecipient();
        String content = request.getTrimmedContent();
        
        Optional<UserDto> recipientUser = userRepository.findByUsername(recipient);
        if (recipientUser.isEmpty()) {
            throw new IllegalArgumentException("Recipient not found: " + recipient);
        }
        
        // Create thread ID (sorted usernames to ensure consistency)
        String threadId = createThreadId(sender, recipient);
        
        // Generate a temporary message ID for immediate response
        String tempMessageId = UUID.randomUUID().toString();
        
        MessageCommand messageCommand = MessageCommand.create(
            tempMessageId, threadId, sender, recipient, content);
        
        kafkaProducerService.sendMessageCommand(messageCommand);
        
        log.info("Message command sent to Kafka from {} to {} in thread {}", sender, recipient, threadId);
        

        MessageDto tempMessage = new MessageDto(threadId, sender, content);
        tempMessage.setId(tempMessageId);
        tempMessage.setStatus("pending");
        
        return tempMessage;
    }
    
    public List<MessageDto> getConversation(String username1, String username2) {
        String user1 = username1.trim().toLowerCase();
        String user2 = username2.trim().toLowerCase();
        String threadId = createThreadId(user1, user2);
        
        return messageRepository.findByThreadIdOrderByTimestampAsc(threadId);
    }
    
    public List<MessageDto> getUserMessages(String username) {
        String user = username.trim().toLowerCase();
        return messageRepository.findBySenderOrderByTimestampDesc(user);
    }
    
    private String createThreadId(String username1, String username2) {
        List<String> users = Arrays.asList(username1, username2);
        Collections.sort(users);
        return String.join("-", users);
    }
}
