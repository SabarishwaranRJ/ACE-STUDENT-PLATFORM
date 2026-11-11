package com.aceteamup.repository;

import com.aceteamup.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Skill}.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /** Find all skills belonging to a specific category (e.g., "AI/ML", "Frontend"). */
    List<Skill> findBySkillCategory(String skillCategory);

    Optional<Skill> findBySkillName(String skillName);

    boolean existsBySkillName(String skillName);
}

