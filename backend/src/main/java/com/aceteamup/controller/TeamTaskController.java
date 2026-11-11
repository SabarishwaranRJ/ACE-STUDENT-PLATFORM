package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.workspace.TeamTaskRequest;
import com.aceteamup.dto.workspace.TeamTaskResponse;
import com.aceteamup.dto.workspace.UpdateTaskStatusRequest;
import com.aceteamup.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/tasks")
public class TeamTaskController {

    private final TaskService taskService;

    public TeamTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamTaskResponse>>> getTasks(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TeamTaskResponse> tasks = taskService.getTeamTasks(userDetails.getUsername(), teamId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamTaskResponse>> createTask(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamTaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamTaskResponse response = taskService.createTask(userDetails.getUsername(), teamId, req);
        return ResponseEntity.ok(ApiResponse.success("Task created", response));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TeamTaskResponse>> updateTask(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @Valid @RequestBody TeamTaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamTaskResponse response = taskService.updateTask(userDetails.getUsername(), teamId, taskId, req);
        return ResponseEntity.ok(ApiResponse.success("Task updated", response));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TeamTaskResponse>> updateTaskStatus(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        TeamTaskResponse response = taskService.updateTaskStatus(userDetails.getUsername(), teamId, taskId, req);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(userDetails.getUsername(), teamId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }
}

