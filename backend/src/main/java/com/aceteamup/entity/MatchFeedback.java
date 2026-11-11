package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for the {@code match_feedback} table.
 * Records explicit user feedback on AI-generated recommendations.
 * Used to retrain and refine the matching model.
 */
@Entity
@Table(
    name = "match_feedback",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_mf_student_recommended_event",
        columnNames = {"student_id", "recommended_student_id", "event_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchFeedback {

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum FeedbackType {
        Like, Dislike, Skip
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false, updatable = false)
    private Long feedbackId;

    /** The student providing the feedback. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** The student about whom feedback is given. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_student_id", nullable = false)
    private Student recommendedStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 10)
    private FeedbackType feedbackType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

