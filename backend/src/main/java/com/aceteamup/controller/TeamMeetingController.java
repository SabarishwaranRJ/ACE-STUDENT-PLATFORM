package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.TeamMeetingRequest;
import com.aceteamup.dto.workspace.TeamMeetingResponse;
import com.aceteamup.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/meetings")
public class TeamMeetingController {

    private final MeetingService meetingService;

    public TeamMeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamMeetingResponse>>> getMeetings(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamMeetingResponse> meetings = meetingService.getTeamMeetings(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success(meetings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamMeetingResponse>> scheduleMeeting(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamMeetingRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamMeetingResponse response = meetingService.scheduleMeeting(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Meeting scheduled", response));
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<ApiResponse<Void>> cancelMeeting(
            @PathVariable Long teamId,
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        meetingService.cancelMeeting(userDetails.getUsername(), teamId, meetingId);
        return ResponseEntity.ok(ApiResponse.success("Meeting cancelled", null));
    }
}

