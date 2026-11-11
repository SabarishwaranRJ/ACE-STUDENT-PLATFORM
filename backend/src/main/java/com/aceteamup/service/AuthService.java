package com.aceteamup.service;

import com.aceteamup.dto.auth.AuthResponse;
import com.aceteamup.dto.auth.LoginRequest;
import com.aceteamup.dto.auth.RegisterRequest;
import com.aceteamup.entity.College;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Student.AcademicYear;
import com.aceteamup.entity.Student.AvailabilityType;
import com.aceteamup.entity.Student.ExperienceLevel;
import com.aceteamup.entity.Student.ProfileVisibility;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.CollegeRepository;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(StudentRepository studentRepository,
                       CollegeRepository collegeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (studentRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + req.getEmail());
        }

        College college = null;
        if (req.getCollegeId() != null) {
            college = collegeRepository.findById(req.getCollegeId()).orElse(null);
        }

        Student student = Student.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .college(college)
                .department(req.getDepartment())
                .academicYear(AcademicYear.fromValue(req.getAcademicYear()))
                .bio(req.getBio())
                .preferredRole(req.getPreferredRole())
                .experienceLevel(parseExperienceLevel(req.getExperienceLevel()))
                .availability(AvailabilityType.fromValue(req.getAvailability()))
                .profileVisibility(parseProfileVisibility(req.getProfileVisibility()))
                .build();

        Student saved = studentRepository.save(student);
        String token = jwtUtil.generateToken(saved.getEmail());

        return AuthResponse.builder()
                .token(token)
                .studentId(saved.getStudentId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role("STUDENT")
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        Student student = studentRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), student.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(student.getEmail());

        return AuthResponse.builder()
                .token(token)
                .studentId(student.getStudentId())
                .name(student.getName())
                .email(student.getEmail())
                .role("STUDENT")
                .build();
    }

    private ExperienceLevel parseExperienceLevel(String level) {
        if (level == null) return null;
        try {
            return ExperienceLevel.valueOf(level);
        } catch (Exception e) {
            return null;
        }
    }

    private ProfileVisibility parseProfileVisibility(String vis) {
        if (vis == null) return ProfileVisibility.Public;
        try {
            return ProfileVisibility.valueOf(vis);
        } catch (Exception e) {
            return ProfileVisibility.Public;
        }
    }
}
