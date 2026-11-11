package com.aceteamup.service;

import com.aceteamup.dto.availability.AvailabilityRequest;
import com.aceteamup.dto.availability.AvailabilityResponse;
import com.aceteamup.dto.interest.InterestResponse;
import com.aceteamup.dto.skill.StudentSkillRequest;
import com.aceteamup.dto.skill.StudentSkillResponse;
import com.aceteamup.dto.student.StudentProfileResponse;
import com.aceteamup.dto.student.StudentSummaryResponse;
import com.aceteamup.dto.student.UpdateProfileRequest;
import com.aceteamup.entity.*;
import com.aceteamup.entity.Student.AcademicYear;
import com.aceteamup.entity.Student.AvailabilityType;
import com.aceteamup.entity.Student.ExperienceLevel;
import com.aceteamup.entity.Student.ProfileVisibility;
import com.aceteamup.entity.StudentAvailability.AvailabilityDay;
import com.aceteamup.entity.StudentInterest.StudentInterestId;
import com.aceteamup.entity.StudentSkill.ProficiencyLevel;
import com.aceteamup.entity.StudentSkill.StudentSkillId;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final InterestRepository interestRepository;
    private final StudentInterestRepository studentInterestRepository;
    private final StudentAvailabilityRepository studentAvailabilityRepository;

    public StudentService(StudentRepository studentRepository,
                          CollegeRepository collegeRepository,
                          SkillRepository skillRepository,
                          StudentSkillRepository studentSkillRepository,
                          InterestRepository interestRepository,
                          StudentInterestRepository studentInterestRepository,
                          StudentAvailabilityRepository studentAvailabilityRepository) {
        this.studentRepository = studentRepository;
        this.collegeRepository = collegeRepository;
        this.skillRepository = skillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.interestRepository = interestRepository;
        this.studentInterestRepository = studentInterestRepository;
        this.studentAvailabilityRepository = studentAvailabilityRepository;
    }

    public StudentProfileResponse getProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        return mapToProfileResponse(student);
    }

    public StudentProfileResponse getMyProfile(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        return mapToProfileResponse(student);
    }

    @Transactional
    public StudentProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        if (req.getName() != null) student.setName(req.getName());
        if (req.getBio() != null) student.setBio(req.getBio());
        if (req.getDepartment() != null) student.setDepartment(req.getDepartment());
        if (req.getPreferredRole() != null) student.setPreferredRole(req.getPreferredRole());

        if (req.getAcademicYear() != null) {
            student.setAcademicYear(AcademicYear.fromValue(req.getAcademicYear()));
        }
        if (req.getExperienceLevel() != null) {
            try {
                student.setExperienceLevel(ExperienceLevel.valueOf(req.getExperienceLevel()));
            } catch (Exception ignored) {}
        }
        if (req.getAvailability() != null) {
            student.setAvailability(AvailabilityType.fromValue(req.getAvailability()));
        }
        if (req.getProfileVisibility() != null) {
            try {
                student.setProfileVisibility(ProfileVisibility.valueOf(req.getProfileVisibility()));
            } catch (Exception ignored) {}
        }
        if (req.getCollegeId() != null) {
            collegeRepository.findById(req.getCollegeId()).ifPresent(student::setCollege);
        }

        Student updated = studentRepository.save(student);
        return mapToProfileResponse(updated);
    }

    public List<StudentSummaryResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .filter(s -> s.getProfileVisibility() != ProfileVisibility.Private)
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    // Skills
    public List<StudentSkillResponse> getStudentSkills(Long studentId) {
        return studentSkillRepository.findByStudentStudentId(studentId).stream()
                .map(ss -> StudentSkillResponse.builder()
                        .skillId(ss.getSkill().getSkillId())
                        .skillName(ss.getSkill().getSkillName())
                        .skillCategory(ss.getSkill().getSkillCategory())
                        .proficiencyLevel(ss.getProficiencyLevel() != null ? ss.getProficiencyLevel().name() : null)
                        .yearsOfExperience(ss.getYearsOfExperience())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentSkillResponse addOrUpdateSkill(String email, StudentSkillRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        Skill skill;
        if (req.getSkillId() != null && req.getSkillId() > 0) {
            skill = skillRepository.findById(req.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + req.getSkillId()));
        } else if (req.getSkillName() != null && !req.getSkillName().isBlank()) {
            String trimmedName = req.getSkillName().trim();
            skill = skillRepository.findBySkillName(trimmedName)
                    .orElseGet(() -> skillRepository.save(Skill.builder()
                            .skillName(trimmedName)
                            .skillCategory(req.getSkillCategory() != null && !req.getSkillCategory().isBlank()
                                    ? req.getSkillCategory()
                                    : "Technical")
                            .build()));
        } else {
            throw new com.aceteamup.exception.BadRequestException("Skill ID or Skill Name is required");
        }

        StudentSkillId id = new StudentSkillId(student.getStudentId(), skill.getSkillId());
        ProficiencyLevel level = ProficiencyLevel.Beginner;
        try {
            level = ProficiencyLevel.valueOf(req.getProficiencyLevel());
        } catch (Exception ignored) {}

        StudentSkill studentSkill = StudentSkill.builder()
                .id(id)
                .student(student)
                .skill(skill)
                .proficiencyLevel(level)
                .yearsOfExperience(req.getYearsOfExperience())
                .build();

        StudentSkill saved = studentSkillRepository.save(studentSkill);
        return StudentSkillResponse.builder()
                .skillId(saved.getSkill().getSkillId())
                .skillName(saved.getSkill().getSkillName())
                .skillCategory(saved.getSkill().getSkillCategory())
                .proficiencyLevel(saved.getProficiencyLevel().name())
                .yearsOfExperience(saved.getYearsOfExperience())
                .build();
    }

    @Transactional
    public void removeSkill(String email, Long skillId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        studentSkillRepository.deleteByStudentStudentIdAndSkillSkillId(student.getStudentId(), skillId);
    }

    // Interests
    public List<InterestResponse> getStudentInterests(Long studentId) {
        return studentInterestRepository.findByStudentStudentId(studentId).stream()
                .map(si -> InterestResponse.builder()
                        .interestId(si.getInterest().getInterestId())
                        .interestName(si.getInterest().getInterestName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void addInterest(String email, Long interestId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found: " + interestId));

        StudentInterestId id = new StudentInterestId(student.getStudentId(), interest.getInterestId());
        StudentInterest studentInterest = StudentInterest.builder()
                .id(id)
                .student(student)
                .interest(interest)
                .build();

        studentInterestRepository.save(studentInterest);
    }

    @Transactional
    public void removeInterest(String email, Long interestId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        studentInterestRepository.deleteByStudentStudentIdAndInterestInterestId(student.getStudentId(), interestId);
    }

    // Availability
    public List<AvailabilityResponse> getStudentAvailability(Long studentId) {
        return studentAvailabilityRepository.findByStudentStudentId(studentId).stream()
                .map(sa -> AvailabilityResponse.builder()
                        .availabilityId(sa.getAvailabilityId())
                        .dayOfWeek(sa.getDayOfWeek() != null ? sa.getDayOfWeek().name() : null)
                        .startTime(sa.getStartTime())
                        .endTime(sa.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public AvailabilityResponse addAvailability(String email, AvailabilityRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        AvailabilityDay day = AvailabilityDay.valueOf(req.getDayOfWeek());

        StudentAvailability availability = StudentAvailability.builder()
                .student(student)
                .dayOfWeek(day)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .build();

        StudentAvailability saved = studentAvailabilityRepository.save(availability);
        return AvailabilityResponse.builder()
                .availabilityId(saved.getAvailabilityId())
                .dayOfWeek(saved.getDayOfWeek().name())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build();
    }

    @Transactional
    public void removeAvailability(String email, Long availabilityId) {
        studentAvailabilityRepository.deleteById(availabilityId);
    }

    // Helpers
    private StudentProfileResponse mapToProfileResponse(Student s) {
        return StudentProfileResponse.builder()
                .studentId(s.getStudentId())
                .name(s.getName())
                .email(s.getEmail())
                .collegeId(s.getCollege() != null ? s.getCollege().getCollegeId() : null)
                .collegeName(s.getCollege() != null ? s.getCollege().getCollegeName() : null)
                .department(s.getDepartment())
                .academicYear(s.getAcademicYear() != null ? s.getAcademicYear().getDbValue() : null)
                .bio(s.getBio())
                .preferredRole(s.getPreferredRole())
                .experienceLevel(s.getExperienceLevel() != null ? s.getExperienceLevel().name() : null)
                .availability(s.getAvailability() != null ? s.getAvailability().getDbValue() : null)
                .profileVisibility(s.getProfileVisibility() != null ? s.getProfileVisibility().name() : "Public")
                .createdAt(s.getCreatedAt())
                .build();
    }

    public StudentSummaryResponse mapToSummaryResponse(Student s) {
        return StudentSummaryResponse.builder()
                .studentId(s.getStudentId())
                .name(s.getName())
                .email(s.getEmail())
                .collegeName(s.getCollege() != null ? s.getCollege().getCollegeName() : null)
                .department(s.getDepartment())
                .academicYear(s.getAcademicYear() != null ? s.getAcademicYear().getDbValue() : null)
                .preferredRole(s.getPreferredRole())
                .experienceLevel(s.getExperienceLevel() != null ? s.getExperienceLevel().name() : null)
                .build();
    }
}
