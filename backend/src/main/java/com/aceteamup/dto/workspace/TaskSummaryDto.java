package com.aceteamup.dto.workspace;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSummaryDto {
    private long totalTasks;
    private long todoCount;
    private long inProgressCount;
    private long inReviewCount;
    private long completedCount;
    private int completionPercentage;
}

