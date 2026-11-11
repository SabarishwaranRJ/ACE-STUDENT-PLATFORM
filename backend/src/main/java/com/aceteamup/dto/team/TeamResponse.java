package com.aceteamup.dto.team;

import com.aceteamup.dto.event.EventResponse;
import com.aceteamup.dto.student.StudentSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {
    private Long teamId;
    private String teamName;
    private EventResponse event;
    private StudentSummaryResponse leader;
    private String description;
    private String teamStatus;
    private int memberCount;
    private LocalDateTime createdAt;
}
