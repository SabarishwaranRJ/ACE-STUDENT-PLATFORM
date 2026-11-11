package com.aceteamup.repository;

import com.aceteamup.entity.TeamFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamFileRepository extends JpaRepository<TeamFile, Long> {
    List<TeamFile> findByTeamTeamIdOrderByUploadedAtDesc(Long teamId);
    List<TeamFile> findByTeamTeamId(Long teamId);
    List<TeamFile> findByTeamTeamIdAndCategoryOrderByUploadedAtDesc(Long teamId, String category);
    long countByTeamTeamId(Long teamId);
}

