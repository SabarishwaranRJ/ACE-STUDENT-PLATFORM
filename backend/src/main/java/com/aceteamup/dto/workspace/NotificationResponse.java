package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long notificationId;
    private Long recipientId;
    private String notificationType;
    private String title;
    private String message;
    private Long relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

