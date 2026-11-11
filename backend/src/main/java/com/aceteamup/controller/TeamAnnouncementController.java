package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.TeamAnnouncementRequest;
import com.aceteamup.dto.workspace.TeamAnnouncementResponse;
import com.aceteamup.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/announcements")
public class TeamAnnouncementController {

    private final AnnouncementService announcementService;

    public TeamAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamAnnouncementResponse>>> getAnnouncements(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamAnnouncementResponse> announcements = announcementService.getTeamAnnouncements(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamAnnouncementResponse>> createAnnouncement(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamAnnouncementRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamAnnouncementResponse response = announcementService.createAnnouncement(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Announcement published", response));
    }

    @PutMapping("/{announcementId}")
    public ResponseEntity<ApiResponse<TeamAnnouncementResponse>> updateAnnouncement(
            @PathVariable Long teamId,
            @PathVariable Long announcementId,
            @Valid @RequestBody TeamAnnouncementRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamAnnouncementResponse response = announcementService.updateAnnouncement(userDetails.getUsername(), teamId, announcementId, req);
        return ResponseEntity.ok(ApiResponse.success("Announcement updated", response));
    }

    @PatchMapping("/{announcementId}/pin")
    public ResponseEntity<ApiResponse<TeamAnnouncementResponse>> togglePin(
            @PathVariable Long teamId,
            @PathVariable Long announcementId,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamAnnouncementResponse response = announcementService.togglePin(userDetails.getUsername(), teamId, announcementId);
        return ResponseEntity.ok(ApiResponse.success("Announcement pin updated", response));
    }

    @DeleteMapping("/{announcementId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @PathVariable Long teamId,
            @PathVariable Long announcementId,
            @AuthenticationPrincipal UserDetails userDetails) {
        announcementService.deleteAnnouncement(userDetails.getUsername(), teamId, announcementId);
        return ResponseEntity.ok(ApiResponse.success("Announcement deleted", null));
    }
}

