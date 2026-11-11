package com.aceteamup.repository;

import com.aceteamup.entity.StudentAvailability;
import com.aceteamup.entity.StudentAvailability.AvailabilityDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link StudentAvailability}.
 */
@Repository
public interface StudentAvailabilityRepository extends JpaRepository<StudentAvailability, Long> {

    /**
     * Retrieve all availability slots for a given student.
     * Used to render the student's weekly availability calendar.
     */
    List<StudentAvailability> findByStudentStudentId(Long studentId);

    /**
     * Find all students available on a specific day.
     * Used by the team scheduling assistant to suggest meeting slots.
     */
    List<StudentAvailability> findByDayOfWeek(AvailabilityDay dayOfWeek);

    /**
     * Find availability slots that overlap with a given time range on a given day.
     * Overlap condition: start_time <= rangeEnd AND end_time >= rangeStart.
     * Used for scheduling conflict detection.
     */
    List<StudentAvailability> findByDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            AvailabilityDay dayOfWeek, LocalTime rangeEnd, LocalTime rangeStart);
}

