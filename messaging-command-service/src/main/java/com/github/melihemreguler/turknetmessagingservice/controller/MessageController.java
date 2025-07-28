package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.model.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.ConversationRequest;
import com.github.melihemreguler.turknetmessagingservice.model.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String SESSION_TOKEN_HEADER = "X-Session-Token";
    
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @RequestBody @Valid MessageRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        // Get the authenticated user from the request attribute (set by interceptor)
        String senderId = (String) httpRequest.getAttribute("currentUser");
        
        log.info("Message send request from user ID {} to {}", senderId, request.recipient());
        MessageDto message = messageService.sendMessage(senderId, request);
        
        // Add session token back to response header
        String sessionToken = httpRequest.getHeader(SESSION_TOKEN_HEADER);
        if (sessionToken != null) {
            httpResponse.addHeader(SESSION_TOKEN_HEADER, sessionToken);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", message));
    }
    
    @PostMapping("/conversation")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getConversation(
            @RequestBody @Valid ConversationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        // Get the authenticated user from the request attribute (set by interceptor)
        String currentUserId = (String) httpRequest.getAttribute("currentUser");
        
        // Ensure the current user is part of the conversation
        if (!currentUserId.equals(request.user1()) && !currentUserId.equals(request.user2())) {
            log.warn("User {} attempted to access conversation between {} and {}", 
                    currentUserId, request.user1(), request.user2());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this conversation"));
        }
        
        log.info("Fetching conversation between {} and {}", request.user1(), request.user2());
        List<MessageDto> messages = messageService.getConversation(request.user1(), request.user2());
        
        // Add session token back to response header
        String sessionToken = httpRequest.getHeader(SESSION_TOKEN_HEADER);
        if (sessionToken != null) {
            httpResponse.addHeader(SESSION_TOKEN_HEADER, sessionToken);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved successfully", messages));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getUserMessages(
            @PathVariable String userId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        // Get the authenticated user from the request attribute (set by interceptor)
        String currentUserId = (String) httpRequest.getAttribute("currentUser");
        
        // Ensure users can only access their own messages
        if (!currentUserId.equals(userId)) {
            log.warn("User {} attempted to access messages for user {}", currentUserId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to these messages"));
        }
        
        log.info("Fetching messages for user ID: {}", userId);
        List<MessageDto> messages = messageService.getUserMessages(userId);
        
        // Add session token back to response header
        String sessionToken = httpRequest.getHeader(SESSION_TOKEN_HEADER);
        if (sessionToken != null) {
            httpResponse.addHeader(SESSION_TOKEN_HEADER, sessionToken);
        }
        
        return ResponseEntity.ok(ApiResponse.success("User messages retrieved successfully", messages));
    }
}
