package com.aceteamup.dto.skill;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkillRequest {
    private Long skillId;

    private String skillName;

    private String skillCategory;

    @NotNull(message = "Proficiency level is required")
    private String proficiencyLevel; // Beginner, Intermediate, Advanced, Expert

    private BigDecimal yearsOfExperience;
}
