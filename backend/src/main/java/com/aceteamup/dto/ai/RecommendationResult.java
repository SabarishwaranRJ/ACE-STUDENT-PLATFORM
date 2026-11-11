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
public class RecommendationResult {
    private Long recommendedStudentId;
    private String name;
    private double compatibilityScore;
    private String explanation;
    private List<String> skillMatches;
    private List<String> complementarySkills;
    private List<String> missingSkills;
    private Map<String, Double> scoreBreakdown;
}
