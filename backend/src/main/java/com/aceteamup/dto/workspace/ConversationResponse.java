package com.aceteamup.dto.workspace;

import com.aceteamup.dto.student.StudentSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private Long conversationId;
    private StudentSummaryResponse otherParticipant;
    private DirectMessageResponse lastMessage;
    private long unreadCount;
    private LocalDateTime updatedAt;
}

