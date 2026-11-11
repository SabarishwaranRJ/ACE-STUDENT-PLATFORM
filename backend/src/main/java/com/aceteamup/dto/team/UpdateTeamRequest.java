package com.aceteamup.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTeamRequest {
    private String teamName;
    private String description;
    private String teamStatus; // Open, Full, Closed, Disbanded
}
