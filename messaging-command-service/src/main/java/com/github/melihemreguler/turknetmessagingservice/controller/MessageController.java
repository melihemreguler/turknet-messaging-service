package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.enums.SessionConstants;
import com.github.melihemreguler.turknetmessagingservice.model.api.*;
import com.github.melihemreguler.turknetmessagingservice.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @RequestBody @Valid MessageRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String senderId = (String) httpRequest.getAttribute(SessionConstants.USER_ID_ATTRIBUTE.toString());
        
        log.info("Message send request from user ID {} to {}", senderId, request.recipient());
        MessageDto message = messageService.sendMessage(senderId, request);
        
        MessageResponse responseData = new MessageResponse(
            message.getThreadId(),
            message.getSenderId(),
            message.getContent(),
            message.getTimestamp()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", responseData));
    }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PaginatedResponse<MessageDto>>> getConversation(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String currentUserId = (String) httpRequest.getAttribute(SessionConstants.USER_ID_ATTRIBUTE.toString());
        
        // Validate that at least one of userId or username is provided
        if ((userId == null || userId.trim().isEmpty()) && (username == null || username.trim().isEmpty())) {
            log.warn("User {} attempted to fetch conversation without providing userId or username", currentUserId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Either userId or username parameter must be provided"));
        }
        
        // Create ConversationRequest with current user as user1 and target user as user2
        HistoryRequest request = new HistoryRequest(
            currentUserId,    // user1Id - current user from interceptor
            userId,          // user2Id - target user from query param
            username,        // user2Username - target username from query param  
            limit, 
            offset
        );
        
        // Get security info to validate access
        MessageService.ConversationSecurityInfo securityInfo = messageService.getConversationSecurityInfo(request);
        
        // Ensure the current user is part of the conversation (should always be true since we set user1 as current user)
        if (!securityInfo.isUserPartOfConversation(currentUserId)) {
            log.warn("User {} attempted to access conversation between {} and {}", 
                    currentUserId, securityInfo.getUser1Id(), securityInfo.getUser2Id());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this conversation"));
        }
        
        log.info("Fetching conversation between current user {} and target user {} (limit: {}, offset: {})", 
                currentUserId, securityInfo.getUser2Id(), request.limit(), request.offset());
        
        PaginatedResponse<MessageDto> paginatedMessages = messageService.getConversationPaginated(request);
        
        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved successfully", paginatedMessages));
    }
}
