package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.model.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @RequestParam String sender,
            @RequestBody @Valid MessageRequest request) {
        
        try {
            log.info("Message send request from {} to {}", sender, request.recipient());
            MessageDto message = messageService.sendMessage(sender, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Message sent successfully", message));
        } catch (IllegalArgumentException e) {
            log.warn("Message send failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during message send: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
    
    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getConversation(
            @RequestParam String user1,
            @RequestParam String user2) {
        
        try {
            log.info("Fetching conversation between {} and {}", user1, user2);
            List<MessageDto> messages = messageService.getConversation(user1, user2);
            
            return ResponseEntity.ok(ApiResponse.success("Conversation retrieved successfully", messages));
        } catch (Exception e) {
            log.error("Error fetching conversation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getUserMessages(@PathVariable String username) {
        try {
            log.info("Fetching messages for user: {}", username);
            List<MessageDto> messages = messageService.getUserMessages(username);
            
            return ResponseEntity.ok(ApiResponse.success("User messages retrieved successfully", messages));
        } catch (Exception e) {
            log.error("Error fetching user messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error"));
        }
    }
}
