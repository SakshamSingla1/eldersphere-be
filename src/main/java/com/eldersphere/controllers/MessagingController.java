package com.eldersphere.controllers;

import com.eldersphere.dtos.Messaging.ConversationCreateRequest;
import com.eldersphere.dtos.Messaging.ConversationResponse;
import com.eldersphere.dtos.Messaging.MessageRequest;
import com.eldersphere.dtos.Messaging.MessageResponse;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.MessagingService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Messaging", description = "In-app conversations and messages between two users, delivered in real time over WebSocket (/ws, topic /topic/conversations/{id})")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService messagingService;
    private final Helper helper;

    @Operation(summary = "Get or create a conversation", description = "Returns the existing conversation between the caller and the given user (optionally scoped to a booking), creating one if it doesn't exist yet.")
    @PostMapping
    public ResponseEntity<ResponseModel<ConversationResponse>> getOrCreate(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody ConversationCreateRequest request) throws GenericException {
        Long callerId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(messagingService.getOrCreateConversation(callerId, request), "Conversation ready");
    }

    @Operation(summary = "List my conversations", description = "Most recently active first.")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<ConversationResponse>>> getMine(
            @RequestHeader(value = "Authorization", required = false) String auth, Pageable pageable) throws GenericException {
        Long callerId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(messagingService.getConversationsForUser(callerId, pageable), "Conversations fetched successfully");
    }

    @Operation(summary = "List messages in a conversation", description = "Paged, oldest first. Only the two participants (or an admin) may access this. Pass beforeId (a message id) instead of page/size to cursor-load the messages immediately preceding it, for infinite-scroll \"load older\".")
    @GetMapping("/{id}/messages")
    public ResponseEntity<ResponseModel<Page<MessageResponse>>> getMessages(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestParam(required = false) Long beforeId,
            Pageable pageable) throws GenericException {
        Long callerId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(messagingService.getMessages(callerId, id, beforeId, pageable), "Messages fetched successfully");
    }

    @Operation(summary = "Send a message", description = "Persists the message, broadcasts it to /topic/conversations/{id}, and notifies the other participant.")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ResponseModel<MessageResponse>> sendMessage(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id, @Valid @RequestBody MessageRequest request) throws GenericException {
        Long callerId = helper.getUserIdFromHeader(auth);
        return ApiResponse.createSuccess(messagingService.sendMessage(callerId, id, request), "Message sent successfully");
    }

    @Operation(summary = "Mark all messages in a conversation as read", description = "Marks every message sent by the other participant as read by the caller.")
    @PutMapping("/{id}/read")
    public ResponseEntity<ResponseModel<String>> markRead(
            @RequestHeader(value = "Authorization", required = false) String auth, @PathVariable Long id) throws GenericException {
        Long callerId = helper.getUserIdFromHeader(auth);
        messagingService.markRead(callerId, id);
        return ApiResponse.successResponse("Conversation marked as read");
    }
}
