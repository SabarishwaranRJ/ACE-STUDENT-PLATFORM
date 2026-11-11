package com.aceteamup.dto.workspace;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingEventDto {
    private Long teamId;
    private Long studentId;
    private String studentName;
    private boolean isTyping;
}

