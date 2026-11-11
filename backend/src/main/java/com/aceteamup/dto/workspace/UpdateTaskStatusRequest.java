package com.aceteamup.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}

