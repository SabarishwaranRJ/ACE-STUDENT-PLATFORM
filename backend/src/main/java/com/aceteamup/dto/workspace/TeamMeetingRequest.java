package com.aceteamup.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMeetingRequest {
    @NotBlank(message = "Meeting title is required")
    private String title;

    private String description;

    @NotBlank(message = "Meeting link is required")
    private String meetingLink;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}

