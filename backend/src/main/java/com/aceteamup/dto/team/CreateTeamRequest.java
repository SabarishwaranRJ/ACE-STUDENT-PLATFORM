package com.aceteamup.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeamRequest {
    @NotBlank(message = "Team name is required")
    private String teamName;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    private String description;
}
