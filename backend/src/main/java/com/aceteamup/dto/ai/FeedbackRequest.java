package com.aceteamup.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackRequest {
    @NotNull(message = "Recommended Student ID is required")
    private Long recommendedStudentId;

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Feedback type is required")
    private String feedbackType; // Like, Dislike, Skip
}
