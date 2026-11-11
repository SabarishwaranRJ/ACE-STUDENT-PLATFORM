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
public class OptimizeTeamResponse {
    private List<Long> selectedStudentIds;
    private List<Map<String, Object>> selectedStudents;
    private boolean feasible;
    private String explanation;
    private double objectiveValue;
    private double skillCoverage;
    private double processingTimeMs;
}
