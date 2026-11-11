package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.invitation.InvitationResponse;
import com.aceteamup.dto.invitation.RespondInvitationRequest;
import com.aceteamup.dto.invitation.SendInvitationRequest;
import com.aceteamup.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvitationResponse>> sendInvitation(@AuthenticationPrincipal UserDetails userDetails,
                                                                          @Valid @RequestBody SendInvitationRequest req) {
        InvitationResponse res = invitationService.sendInvitation(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent", res));
    }

    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvitationResponse> res = invitationService.getMyInvitations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getSentInvitations(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvitationResponse> res = invitationService.getSentInvitations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<InvitationResponse>> respondToInvitation(@AuthenticationPrincipal UserDetails userDetails,
                                                                               @PathVariable Long id,
                                                                               @RequestBody RespondInvitationRequest req) {
        InvitationResponse res = invitationService.respondToInvitation(userDetails.getUsername(), id, req);
        return ResponseEntity.ok(ApiResponse.success("Response recorded", res));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawInvitation(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long id) {
        invitationService.withdrawInvitation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Invitation withdrawn", null));
    }
}
