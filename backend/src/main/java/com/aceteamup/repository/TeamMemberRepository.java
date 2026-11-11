package com.aceteamup.repository;

import com.aceteamup.entity.TeamMember;
import com.aceteamup.entity.TeamMember.TeamMemberId;
import com.aceteamup.entity.TeamMember.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TeamMember}.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

    /**
     * Check whether a student is already a member of any team for a given event.
     * Uses the denormalized {@code event_id} column.
     * Business rule: a student may not join two teams in the same event.
     */
    Optional<TeamMember> findByStudentStudentIdAndEventId(Long studentId, Long eventId);

    /**
     * List all membership records for a team (all statuses).
     * Use to build team roster views.
     */
    List<TeamMember> findByTeamTeamId(Long teamId);

    /**
     * List active members of a team.
     */
    List<TeamMember> findByTeamTeamIdAndMembershipStatus(Long teamId, MembershipStatus status);

    /**
     * List all team memberships (across all events) for a student.
     */
    List<TeamMember> findByStudentStudentId(Long studentId);
}

