package com.aceteamup.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkillResponse {
    private Long skillId;
    private String skillName;
    private String skillCategory;
    private String proficiencyLevel;
    private BigDecimal yearsOfExperience;
}
