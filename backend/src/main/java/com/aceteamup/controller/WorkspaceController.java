package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.WorkspaceOverviewResponse;
import com.aceteamup.service.WorkspaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WorkspaceOverviewResponse>> getWorkspaceOverview(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        WorkspaceOverviewResponse response = workspaceService.getWorkspaceOverview(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

