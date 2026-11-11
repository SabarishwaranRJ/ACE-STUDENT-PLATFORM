package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * JPA Entity for the {@code student_availability} table.
 * Records weekly time-slot availability for collaborative scheduling.
 *
 * <p>The inner enum {@link AvailabilityDay} is intentionally named differently
 * from {@link java.time.DayOfWeek} to avoid import ambiguity and to match
 * the MySQL ENUM values exactly via {@code @Enumerated(EnumType.STRING)}.</p>
 */
@Entity
@Table(
    name = "student_availability",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_sa_student_day_start",
        columnNames = {"student_id", "day_of_week", "start_time"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAvailability {

    // -------------------------------------------------------------------------
    // Inner enum  — named AvailabilityDay to avoid clash with java.time.DayOfWeek
    // -------------------------------------------------------------------------

    public enum AvailabilityDay {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id", nullable = false, updatable = false)
    private Long availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private AvailabilityDay dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}

