package com.aceteamup.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizeTeamRequest {
    private Long eventId;
    private Long teamId;
    private List<Long> candidateStudentIds;
    @Builder.Default
    private int minTeamSize = 2;
    @Builder.Default
    private int maxTeamSize = 5;
    private List<String> requiredSkills;
}
