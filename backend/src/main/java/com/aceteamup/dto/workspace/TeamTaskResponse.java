package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamTaskResponse {
    private Long taskId;
    private Long teamId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

