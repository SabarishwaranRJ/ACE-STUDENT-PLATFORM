package com.aceteamup.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSummaryResponse {
    private Long studentId;
    private String name;
    private String email;
    private String collegeName;
    private String department;
    private String academicYear;
    private String preferredRole;
    private String experienceLevel;
}
