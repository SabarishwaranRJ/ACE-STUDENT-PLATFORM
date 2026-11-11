package com.aceteamup.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRequest {
    private Long studentId;
    private Long eventId;
    @Builder.Default
    private int topK = 10;
}
