package com.aceteamup.dto.invitation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendInvitationRequest {
    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
}
