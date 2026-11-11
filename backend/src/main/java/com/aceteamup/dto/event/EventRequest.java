package com.aceteamup.dto.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {
    @NotBlank(message = "Event name is required")
    private String eventName;
    private String description;
    private LocalDate eventDate;
    private LocalDate registrationDeadline;

    @Min(value = 1, message = "Team size must be at least 1")
    private Integer requiredTeamSize;
    private String eventType; // Hackathon, Datathon, Case_Study, Project, Other
    private String eligibility;
}
