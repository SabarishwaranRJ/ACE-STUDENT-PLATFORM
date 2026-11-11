package com.aceteamup.dto.team;

import com.aceteamup.dto.event.EventResponse;
import com.aceteamup.dto.student.StudentSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDetailResponse {
    private Long teamId;
    private String teamName;
    private EventResponse event;
    private StudentSummaryResponse leader;
    private String description;
    private String teamStatus;
    private int memberCount;
    private LocalDateTime createdAt;
    private List<TeamMemberResponse> members;
    private List<TeamRequirementResponse> requirements;
}
