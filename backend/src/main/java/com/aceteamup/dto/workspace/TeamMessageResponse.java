package com.aceteamup.dto.workspace;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMessageResponse {
    private Long messageId;
    private Long teamId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String messageContent;
    private String messageType;
    private String attachmentUrl;
    private String attachmentName;
    private Long replyToMessageId;
    private LocalDateTime createdAt;
}

