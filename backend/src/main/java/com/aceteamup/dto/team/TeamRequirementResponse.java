package com.aceteamup.dto.team;

import com.aceteamup.dto.skill.SkillResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequirementResponse {
    private Long requirementId;
    private SkillResponse skill;
    private String minimumProficiency;
}
