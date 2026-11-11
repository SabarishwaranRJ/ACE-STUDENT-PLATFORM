package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessageResponse {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String messageContent;
    private String attachmentUrl;
    private String attachmentName;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

