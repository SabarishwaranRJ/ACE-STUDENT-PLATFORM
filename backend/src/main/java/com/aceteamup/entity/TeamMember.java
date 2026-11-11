package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for the {@code team_members} table.
 *
 * <p>The {@code eventId} field is a denormalized copy of {@code teams.event_id}.
 * It exists solely to enforce the database-level unique constraint
 * {@code UNIQUE(student_id, event_id)}, which prevents a student from
 * joining two different teams for the same event.</p>
 *
 * <p>Application code is responsible for populating {@code eventId} from
 * the associated {@link Team#getEvent()} when persisting a new member.</p>
 */
@Entity
@Table(
    name = "team_members",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tm_student_event",
        columnNames = {"student_id", "event_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    // -------------------------------------------------------------------------
    // Composite Primary Key
    // -------------------------------------------------------------------------

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberId implements Serializable {

        @Column(name = "team_id", nullable = false)
        private Long teamId;

        @Column(name = "student_id", nullable = false)
        private Long studentId;
    }

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum MembershipStatus {
        Active, Pending, Left, Removed
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @EmbeddedId
    private TeamMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Denormalized copy of the team's event_id.
     * Needed to enforce the UNIQUE(student_id, event_id) constraint at DB level.
     */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "assigned_role", length = 100)
    private String assignedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 20)
    @Builder.Default
    private MembershipStatus membershipStatus = MembershipStatus.Active;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}

