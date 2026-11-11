package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.TeamMessageRequest;
import com.aceteamup.dto.workspace.TeamMessageResponse;
import com.aceteamup.service.ChatService;
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
public class TeamChatController {

    private final ChatService chatService;

    public TeamChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // -------------------------------------------------------------------------
    // REST Endpoints
    // -------------------------------------------------------------------------

    @GetMapping("/api/teams/{teamId}/messages")
    public ResponseEntity<ApiResponse<List<TeamMessageResponse>>> getMessages(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamMessageResponse> messages = chatService.getMessageHistory(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/api/teams/{teamId}/messages")
    public ResponseEntity<ApiResponse<TeamMessageResponse>> postMessage(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamMessageRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamMessageResponse message = chatService.sendMessage(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Message sent", message));
    }

    @DeleteMapping("/api/teams/{teamId}/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long teamId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.deleteMessage(userDetails.getUsername(), teamId, messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted", null));
    }

    // -------------------------------------------------------------------------
    // WebSocket STOMP Message Mapping Endpoints
    // -------------------------------------------------------------------------

    @MessageMapping("/chat.send/{teamId}")
    public void handleStompMessage(@DestinationVariable Long teamId,
                                  @Payload TeamMessageRequest req,
                                  Principal principal) {
        if (principal != null) {
            chatService.sendMessage(principal.getName(), teamId, req);
        }
    }

    @MessageMapping("/chat.typing/{teamId}")
    public void handleStompTyping(@DestinationVariable Long teamId,
                                  @Payload Boolean isTyping,
                                  Principal principal) {
        if (principal != null) {
            chatService.broadcastTyping(principal.getName(), teamId, Boolean.TRUE.equals(isTyping));
        }
    }
}

