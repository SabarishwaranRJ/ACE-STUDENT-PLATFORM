package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity for the {@code skills} table.
 * Master catalogue of technical and soft skills.
 */
@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id", nullable = false, updatable = false)
    private Long skillId;

    @Column(name = "skill_name", nullable = false, unique = true, length = 150)
    private String skillName;

    @Column(name = "skill_category", length = 100)
    private String skillCategory;
}

