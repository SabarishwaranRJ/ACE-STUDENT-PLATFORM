package com.aceteamup.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessageRequest {
    @NotBlank(message = "Message content cannot be blank")
    private String messageContent;

    private String attachmentUrl;

    private String attachmentName;
}

