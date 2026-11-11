package com.aceteamup.repository;

import com.aceteamup.entity.Team;
import com.aceteamup.entity.Team.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Team}.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * Find all teams for a given event filtered by status.
     * Most common use case: {@code findByEventEventIdAndTeamStatus(id, TeamStatus.Open)}
     * to list open teams a student can join.
     */
    List<Team> findByEventEventIdAndTeamStatus(Long eventId, TeamStatus teamStatus);

    /**
     * Find all teams led by a specific student.
     * Used on the student's "My Teams" dashboard.
     */
    List<Team> findByLeaderStudentId(Long leaderId);

    /** All teams for an event (regardless of status). */
    List<Team> findByEventEventId(Long eventId);
}

