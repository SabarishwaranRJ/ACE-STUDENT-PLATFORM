package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMeetingResponse {
    private Long meetingId;
    private Long teamId;
    private String title;
    private String description;
    private String meetingLink;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}

