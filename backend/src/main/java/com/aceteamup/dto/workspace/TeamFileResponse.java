package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamFileResponse {
    private Long fileId;
    private Long teamId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private String category;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}

