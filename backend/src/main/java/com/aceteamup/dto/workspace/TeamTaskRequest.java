package com.aceteamup.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamTaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private String priority; // LOW, MEDIUM, HIGH, URGENT

    private String status; // TODO, IN_PROGRESS, IN_REVIEW, COMPLETED

    private LocalDate dueDate;

    private Long assignedToId;
}

