package com.aceteamup.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillGapResponse {
    private List<String> presentSkills;
    private List<String> missingSkills;
    private List<String> underrepresentedSkills;
    private double coveragePercent;
    private List<String> recommendedProfiles;
    private List<Map<String, Object>> candidateMatches;
}
