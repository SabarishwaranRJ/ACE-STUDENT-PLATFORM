package com.aceteamup.dto.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private Long studentId;
    private String name;
    private String email;
    private String assignedRole;
    private String membershipStatus;
    private LocalDateTime joinedAt;
}
