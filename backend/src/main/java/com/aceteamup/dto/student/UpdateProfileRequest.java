package com.aceteamup.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String name;
    private Long collegeId;
    private String department;
    private String academicYear;
    private String bio;
    private String preferredRole;
    private String experienceLevel;
    private String availability;
    private String profileVisibility;
}
