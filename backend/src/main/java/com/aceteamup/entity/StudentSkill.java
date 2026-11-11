package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * JPA Entity for the {@code student_skills} join table.
 * Maps students to skills with proficiency and experience metadata.
 * Uses an @Embeddable composite key (student_id, skill_id).
 */
@Entity
@Table(name = "student_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkill {

    // -------------------------------------------------------------------------
    // Composite Primary Key
    // -------------------------------------------------------------------------

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSkillId implements Serializable {

        @Column(name = "student_id", nullable = false)
        private Long studentId;

        @Column(name = "skill_id", nullable = false)
        private Long skillId;
    }

    // -------------------------------------------------------------------------
    // Inner enum
    // -------------------------------------------------------------------------

    public enum ProficiencyLevel {
        Beginner, Intermediate, Advanced, Expert
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @EmbeddedId
    private StudentSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false, length = 20)
    @Builder.Default
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.Beginner;

    @Column(name = "years_of_experience", precision = 4, scale = 1)
    private BigDecimal yearsOfExperience;
}

