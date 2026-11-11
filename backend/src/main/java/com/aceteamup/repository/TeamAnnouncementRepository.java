package com.aceteamup.repository;

import com.aceteamup.entity.TeamAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAnnouncementRepository extends JpaRepository<TeamAnnouncement, Long> {
    List<TeamAnnouncement> findByTeamTeamIdOrderByIsPinnedDescCreatedAtDesc(Long teamId);
    long countByTeamTeamId(Long teamId);
}

