package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.team.*;
import com.aceteamup.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                                @Valid @RequestBody CreateTeamRequest req) {
        TeamResponse res = teamService.createTeam(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Team created successfully", res));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamDetailResponse>> getTeam(@PathVariable Long teamId) {
        TeamDetailResponse res = teamService.getTeam(teamId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getTeamsForEvent(@PathVariable Long eventId,
                                                                           @RequestParam(required = false) String status) {
        List<TeamResponse> res = teamService.getTeamsForEvent(eventId, status);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMyTeams(@AuthenticationPrincipal UserDetails userDetails) {
        List<TeamResponse> res = teamService.getMyTeams(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long teamId,
                                                                @RequestBody UpdateTeamRequest req) {
        TeamResponse res = teamService.updateTeam(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Team updated", res));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> requestToJoin(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @Valid @RequestBody JoinTeamRequest req) {
        TeamMemberResponse res = teamService.requestToJoin(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Join request submitted", res));
    }

    @PostMapping("/{teamId}/members/{studentId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveMember(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable Long teamId,
                                                           @PathVariable Long studentId) {
        teamService.approveMember(userDetails.getUsername(), teamId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Member approved", null));
    }

    @DeleteMapping("/{teamId}/members/{studentId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long teamId,
                                                          @PathVariable Long studentId) {
        teamService.removeMember(userDetails.getUsername(), teamId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    @PostMapping("/{teamId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                       @PathVariable Long teamId) {
        teamService.leaveTeam(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success("Left the team", null));
    }

    @PostMapping("/{teamId}/requirements")
    public ResponseEntity<ApiResponse<TeamRequirementResponse>> addRequirement(@AuthenticationPrincipal UserDetails userDetails,
                                                                               @PathVariable Long teamId,
                                                                               @Valid @RequestBody TeamRequirementRequest req) {
        TeamRequirementResponse res = teamService.addTeamRequirement(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Requirement added", res));
    }

    @DeleteMapping("/{teamId}/requirements/{reqId}")
    public ResponseEntity<ApiResponse<Void>> removeRequirement(@AuthenticationPrincipal UserDetails userDetails,
                                                               @PathVariable Long teamId,
                                                               @PathVariable Long reqId) {
        teamService.removeTeamRequirement(userDetails.getUsername(), teamId, reqId);
        return ResponseEntity.ok(ApiResponse.success("Requirement removed", null));
    }

    @GetMapping("/{teamId}/requirements")
    public ResponseEntity<ApiResponse<List<TeamRequirementResponse>>> getRequirements(@PathVariable Long teamId) {
        List<TeamRequirementResponse> res = teamService.getTeamRequirements(teamId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
