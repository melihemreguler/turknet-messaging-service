package com.github.melihemreguler.turknetmessagingservice.controller;

import com.github.melihemreguler.turknetmessagingservice.dto.MessageDto;
import com.github.melihemreguler.turknetmessagingservice.enums.SessionConstants;
import com.github.melihemreguler.turknetmessagingservice.model.request.HistoryRequest;
import com.github.melihemreguler.turknetmessagingservice.model.request.MessageRequest;
import com.github.melihemreguler.turknetmessagingservice.model.response.ApiResponse;
import com.github.melihemreguler.turknetmessagingservice.model.response.MessageResponse;
import com.github.melihemreguler.turknetmessagingservice.model.response.PaginatedResponse;
import com.github.melihemreguler.turknetmessagingservice.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @RequestBody @Valid MessageRequest request,
            HttpServletRequest httpRequest) {

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
            HttpServletRequest httpRequest) {

        String currentUserId = (String) httpRequest.getAttribute(SessionConstants.USER_ID_ATTRIBUTE.toString());

        if (isNullOrEmpty(userId) && isNullOrEmpty(username)) {
            log.warn("User {} attempted to fetch conversation without providing userId or username", currentUserId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Either userId or username parameter must be provided"));
        }

        HistoryRequest request = new HistoryRequest(
                currentUserId,
                userId,
                username,
                limit,
                offset
        );

        MessageService.ConversationSecurityInfo securityInfo = messageService.getConversationSecurityInfo(request);

        if (!securityInfo.isUserPartOfConversation(currentUserId)) {
            log.warn("User {} attempted to access conversation between {} and {}",
                    currentUserId, securityInfo.user1Id(), securityInfo.user2Id());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied to this conversation"));
        }

        log.info("Fetching conversation between current user {} and target user {} (limit: {}, offset: {})",
                currentUserId, securityInfo.user2Id(), request.limit(), request.offset());

        PaginatedResponse<MessageDto> paginatedMessages = messageService.getConversationPaginated(request);

        return ResponseEntity.ok(ApiResponse.success("Conversation retrieved successfully", paginatedMessages));
    }


    private boolean isNullOrEmpty(String value) {
        return Objects.isNull(value) || value.trim().isEmpty();
    }
}
