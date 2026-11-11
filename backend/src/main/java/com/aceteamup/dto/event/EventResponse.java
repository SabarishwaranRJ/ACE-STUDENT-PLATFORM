package com.aceteamup.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long eventId;
    private String eventName;
    private String description;
    private LocalDate eventDate;
    private LocalDate registrationDeadline;
    private Integer requiredTeamSize;
    private String eventType;
    private String eligibility;
    private LocalDateTime createdAt;
}
