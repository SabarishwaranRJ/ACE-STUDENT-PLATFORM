package com.aceteamup.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity for the {@code interests} table.
 * Master catalogue of interest/domain areas.
 */
@Entity
@Table(name = "interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id", nullable = false, updatable = false)
    private Long interestId;

    @Column(name = "interest_name", nullable = false, unique = true, length = 150)
    private String interestName;
}

