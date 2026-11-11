package com.aceteamup.dto.workspace;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamAnnouncementResponse {
    private Long announcementId;
    private Long teamId;
    private String title;
    private String content;
    private Boolean isPinned;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

