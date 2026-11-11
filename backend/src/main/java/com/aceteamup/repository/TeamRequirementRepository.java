package com.aceteamup.repository;

import com.aceteamup.entity.TeamRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRequirementRepository extends JpaRepository<TeamRequirement, Long> {
    List<TeamRequirement> findByTeamTeamId(Long teamId);
    Optional<TeamRequirement> findByTeamTeamIdAndRequiredSkillSkillId(Long teamId, Long skillId);
}
