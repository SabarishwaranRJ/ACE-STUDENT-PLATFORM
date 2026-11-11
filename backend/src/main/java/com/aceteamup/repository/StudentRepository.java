package com.aceteamup.repository;

import com.aceteamup.entity.Student;
import com.aceteamup.entity.Student.AcademicYear;
import com.aceteamup.entity.Student.ExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Student}.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Find a student by their unique email address (used for login). */
    Optional<Student> findByEmail(String email);

    /** Find all students attending a specific college. */
    List<Student> findByCollegeCollegeId(Long collegeId);

    /** Find students in a given department and academic year (e.g., for eligibility checks). */
    List<Student> findByDepartmentAndAcademicYear(String department, AcademicYear academicYear);

    /** Find students at a given experience level (AI matching filter). */
    List<Student> findByExperienceLevel(ExperienceLevel experienceLevel);

    /** Check whether an email is already registered. */
    boolean existsByEmail(String email);
}

