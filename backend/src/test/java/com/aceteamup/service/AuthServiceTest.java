package com.aceteamup.service;

import com.aceteamup.dto.auth.AuthResponse;
import com.aceteamup.dto.auth.LoginRequest;
import com.aceteamup.dto.auth.RegisterRequest;
import com.aceteamup.entity.Student;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.CollegeRepository;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Student student;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .studentId(1L)
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("encodedPassword")
                .build();

        registerRequest = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .department("Computer Science")
                .academicYear("3rd")
                .experienceLevel("Intermediate")
                .availability("Full-time")
                .build();

        loginRequest = LoginRequest.builder()
                .email("alice@example.com")
                .password("password123")
                .build();
    }

    @Test
    void testRegisterSuccess() {
        when(studentRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(jwtUtil.generateToken("alice@example.com")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.register(registerRequest);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("Alice", res.getName());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testRegisterDuplicateEmailThrows() {
        when(studentRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testLoginSuccess() {
        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("alice@example.com")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.login(loginRequest);

        assertNotNull(res);
        assertEquals("mock-jwt-token", res.getToken());
        assertEquals("alice@example.com", res.getEmail());
    }

    @Test
    void testLoginWrongPasswordThrows() {
        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));
    }
}
