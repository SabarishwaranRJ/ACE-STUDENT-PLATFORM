package com.aceteamup.dto.skill;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRequest {
    @NotBlank(message = "Skill name is required")
    private String skillName;
    private String skillCategory;
}
