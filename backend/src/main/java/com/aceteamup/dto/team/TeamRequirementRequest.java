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
public class TeamRequirementRequest {
    @NotNull(message = "Required skill ID is required")
    private Long requiredSkillId;

    private String minimumProficiency; // Beginner, Intermediate, Advanced, Expert
}
