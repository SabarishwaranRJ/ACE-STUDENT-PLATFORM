package com.aceteamup.dto.invitation;

import com.aceteamup.dto.student.StudentSummaryResponse;
import com.aceteamup.dto.team.TeamResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {
    private Long invitationId;
    private TeamResponse team;
    private StudentSummaryResponse sender;
    private StudentSummaryResponse receiver;
    private String invitationStatus;
    private LocalDateTime createdAt;
}
