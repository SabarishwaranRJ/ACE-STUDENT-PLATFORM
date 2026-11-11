package com.aceteamup.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileResponse {
    private Long studentId;
    private String name;
    private String email;
    private Long collegeId;
    private String collegeName;
    private String department;
    private String academicYear;
    private String bio;
    private String preferredRole;
    private String experienceLevel;
    private String availability;
    private String profileVisibility;
    private LocalDateTime createdAt;
}
