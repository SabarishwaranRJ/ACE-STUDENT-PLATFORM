package com.aceteamup.dto.team;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinTeamRequest {
    @NotNull(message = "Team ID is required")
    private Long teamId;
    private String assignedRole;
}
