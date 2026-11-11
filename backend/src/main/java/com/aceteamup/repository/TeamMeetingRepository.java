package com.aceteamup.repository;

import com.aceteamup.entity.TeamMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeamMeetingRepository extends JpaRepository<TeamMeeting, Long> {
    List<TeamMeeting> findByTeamTeamIdOrderByStartTimeDesc(Long teamId);
    List<TeamMeeting> findByTeamTeamIdAndStartTimeAfterOrderByStartTimeAsc(Long teamId, LocalDateTime now);
    long countByTeamTeamId(Long teamId);
}

