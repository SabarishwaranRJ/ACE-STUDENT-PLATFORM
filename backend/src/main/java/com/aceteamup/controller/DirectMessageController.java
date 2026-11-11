package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.ConversationResponse;
import com.aceteamup.dto.workspace.DirectMessageRequest;
import com.aceteamup.dto.workspace.DirectMessageResponse;
import com.aceteamup.service.DirectMessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    public DirectMessageController(DirectMessageService directMessageService) {
        this.directMessageService = directMessageService;
    }

    // -------------------------------------------------------------------------
    // REST Endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/api/conversations/direct/{targetStudentId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateConversation(
            @PathVariable Long targetStudentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ConversationResponse response = directMessageService.getOrCreateConversation(userDetails.getUsername(), targetStudentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ConversationResponse> response = directMessageService.getUserConversations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<DirectMessageResponse>>> getConversationMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DirectMessageResponse> messages = directMessageService.getConversationMessages(userDetails.getUsername(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<DirectMessageResponse>> sendDirectMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody DirectMessageRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        DirectMessageResponse message = directMessageService.sendDirectMessage(userDetails.getUsername(), conversationId, req);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @PatchMapping("/api/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        directMessageService.markAsRead(userDetails.getUsername(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    // -------------------------------------------------------------------------
    // STOMP WebSocket Message Mapping
    // -------------------------------------------------------------------------

    @MessageMapping("/dm.send/{conversationId}")
    public void handleStompDirectMessage(@DestinationVariable Long conversationId,
                                        @Payload DirectMessageRequest req,
                                        Principal principal) {
        if (principal != null) {
            directMessageService.sendDirectMessage(principal.getName(), conversationId, req);
        }
    }
}

