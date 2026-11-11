package com.aceteamup.controller;

import com.aceteamup.dto.ApiResponse;
import com.aceteamup.dto.availability.AvailabilityRequest;
import com.aceteamup.dto.availability.AvailabilityResponse;
import com.aceteamup.dto.interest.InterestResponse;
import com.aceteamup.dto.skill.StudentSkillRequest;
import com.aceteamup.dto.skill.StudentSkillResponse;
import com.aceteamup.dto.student.StudentProfileResponse;
import com.aceteamup.dto.student.StudentSummaryResponse;
import com.aceteamup.dto.student.UpdateProfileRequest;
import com.aceteamup.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        StudentProfileResponse res = studentService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                                               @RequestBody UpdateProfileRequest req) {
        StudentProfileResponse res = studentService.updateProfile(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", res));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(@PathVariable Long id) {
        StudentProfileResponse res = studentService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentSummaryResponse>>> getAllStudents() {
        List<StudentSummaryResponse> res = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    // Skills
    @GetMapping("/me/skills")
    public ResponseEntity<ApiResponse<List<StudentSkillResponse>>> getMySkills(@AuthenticationPrincipal UserDetails userDetails) {
        StudentProfileResponse me = studentService.getMyProfile(userDetails.getUsername());
        List<StudentSkillResponse> res = studentService.getStudentSkills(me.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/me/skills")
    public ResponseEntity<ApiResponse<StudentSkillResponse>> addOrUpdateSkill(@AuthenticationPrincipal UserDetails userDetails,
                                                                              @Valid @RequestBody StudentSkillRequest req) {
        StudentSkillResponse res = studentService.addOrUpdateSkill(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Skill saved", res));
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long skillId) {
        studentService.removeSkill(userDetails.getUsername(), skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }

    // Interests
    @GetMapping("/me/interests")
    public ResponseEntity<ApiResponse<List<InterestResponse>>> getMyInterests(@AuthenticationPrincipal UserDetails userDetails) {
        StudentProfileResponse me = studentService.getMyProfile(userDetails.getUsername());
        List<InterestResponse> res = studentService.getStudentInterests(me.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/me/interests/{interestId}")
    public ResponseEntity<ApiResponse<Void>> addInterest(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long interestId) {
        studentService.addInterest(userDetails.getUsername(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest added", null));
    }

    @DeleteMapping("/me/interests/{interestId}")
    public ResponseEntity<ApiResponse<Void>> removeInterest(@AuthenticationPrincipal UserDetails userDetails,
                                                            @PathVariable Long interestId) {
        studentService.removeInterest(userDetails.getUsername(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest removed", null));
    }

    // Availability
    @GetMapping("/me/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getMyAvailability(@AuthenticationPrincipal UserDetails userDetails) {
        StudentProfileResponse me = studentService.getMyProfile(userDetails.getUsername());
        List<AvailabilityResponse> res = studentService.getStudentAvailability(me.getStudentId());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping("/me/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> addAvailability(@AuthenticationPrincipal UserDetails userDetails,
                                                                             @Valid @RequestBody AvailabilityRequest req) {
        AvailabilityResponse res = studentService.addAvailability(userDetails.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Availability slot added", res));
    }

    @DeleteMapping("/me/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> removeAvailability(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PathVariable Long availabilityId) {
        studentService.removeAvailability(userDetails.getUsername(), availabilityId);
        return ResponseEntity.ok(ApiResponse.success("Availability slot removed", null));
    }
}
