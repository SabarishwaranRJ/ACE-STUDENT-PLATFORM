package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for the {@code invitations} table.
 * Manages the team invitation workflow between students.
 *
 * <p>Business rule: a sender must WITHDRAW any existing {@code Pending}
 * invitation for the same (team, receiver) pair before issuing a new one,
 * because the unique constraint {@code uq_inv_team_receiver} covers all
 * status values (MySQL does not support partial unique indexes).</p>
 */
@Entity
@Table(
    name = "invitations",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_inv_team_receiver",
        columnNames = {"team_id", "receiver_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum InvitationStatus {
        Pending, Accepted, Declined, Withdrawn
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id", nullable = false, updatable = false)
    private Long invitationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** The student who sent the invitation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Student sender;

    /** The student who received the invitation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Student receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus invitationStatus = InvitationStatus.Pending;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

