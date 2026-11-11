package com.aceteamup.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiHealthResponse {
    private String status;
    private boolean modelLoaded;
    private String version;
    private int embeddingDimension;
}
