package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity for the {@code team_requirements} table.
 * Specifies the skills (and minimum proficiency) a team is looking for.
 */
@Entity
@Table(
    name = "team_requirements",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tr_team_skill",
        columnNames = {"team_id", "required_skill_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequirement {

    // -------------------------------------------------------------------------
    // Inner enum (re-uses same values as StudentSkill.ProficiencyLevel)
    // -------------------------------------------------------------------------

    public enum MinimumProficiency {
        Beginner, Intermediate, Advanced, Expert
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requirement_id", nullable = false, updatable = false)
    private Long requirementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_skill_id", nullable = false)
    private Skill requiredSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_proficiency", nullable = false, length = 20)
    @Builder.Default
    private MinimumProficiency minimumProficiency = MinimumProficiency.Beginner;
}

