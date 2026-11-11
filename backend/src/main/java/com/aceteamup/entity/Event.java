package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for the {@code events} table.
 * Represents hackathons, datathons, case studies, and other competitive events.
 */
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum EventType {
        Hackathon,
        Datathon,
        Case_Study,
        Project,
        Other;
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false, updatable = false)
    private Long eventId;

    @Column(name = "event_name", nullable = false, length = 255)
    private String eventName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "required_team_size", columnDefinition = "TINYINT UNSIGNED")
    private Integer requiredTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    @Builder.Default
    private EventType eventType = EventType.Other;

    @Column(name = "eligibility", length = 255)
    private String eligibility;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

