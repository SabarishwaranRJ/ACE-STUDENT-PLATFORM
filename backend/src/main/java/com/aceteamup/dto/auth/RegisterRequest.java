package com.aceteamup.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private Long collegeId;
    private String department;
    private String academicYear; // "1st", "2nd", "3rd", "4th", "Alumni"
    private String bio;
    private String preferredRole;
    private String experienceLevel; // "Beginner", "Intermediate", "Advanced"
    private String availability; // "Full-time", "Part-time", "Weekends"
    private String profileVisibility; // "Public", "Private", "College-only"
}
