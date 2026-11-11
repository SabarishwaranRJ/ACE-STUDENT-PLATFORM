package com.aceteamup.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMessageRequest {
    @NotBlank(message = "Message content cannot be blank")
    private String messageContent;

    private String messageType; // TEXT, FILE, IMAGE, SYSTEM

    private String attachmentUrl;

    private String attachmentName;

    private Long replyToMessageId;
}

