package com.aceteamup.service;

import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamMember;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkspaceSecurityService {

    private final StudentRepository studentRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public WorkspaceSecurityService(StudentRepository studentRepository,
                                  TeamRepository teamRepository,
                                  TeamMemberRepository teamMemberRepository) {
        this.studentRepository = studentRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    /**
     * Validates that the student associated with the email is an active member or leader of the team.
     * Returns the Student entity upon success.
     */
    public Student validateMemberAccess(String email, Long teamId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + teamId));

        if (team.getLeader() != null && team.getLeader().getStudentId().equals(student.getStudentId())) {
            return student;
        }

        TeamMember.TeamMemberId memberId = new TeamMember.TeamMemberId(teamId, student.getStudentId());
        boolean isActive = teamMemberRepository.findById(memberId)
                .map(m -> m.getMembershipStatus() == TeamMember.MembershipStatus.Active)
                .orElse(false);

        if (!isActive) {
            throw new UnauthorizedException("Access denied: You are not an active member of this team workspace.");
        }

        return student;
    }

    /**
     * Checks if the student is the team leader.
     */
    public boolean isTeamLeader(Student student, Team team) {
        return team.getLeader() != null && team.getLeader().getStudentId().equals(student.getStudentId());
    }

    /**
     * Checks if a student is an active member or leader of a team by IDs (used by WebSocket STOMP interceptor).
     */
    public boolean isStudentMemberOfTeam(Long studentId, Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return false;

        if (team.getLeader() != null && team.getLeader().getStudentId().equals(studentId)) {
            return true;
        }

        TeamMember.TeamMemberId memberId = new TeamMember.TeamMemberId(teamId, studentId);
        return teamMemberRepository.findById(memberId)
                .map(m -> m.getMembershipStatus() == TeamMember.MembershipStatus.Active)
                .orElse(false);
    }

    /**
     * Validates that the student is either the team leader or the creator of an entity.
     */
    public void validateLeaderOrCreator(Student student, Team team, Long creatorStudentId, String actionName) {
        boolean isLeader = isTeamLeader(student, team);
        boolean isCreator = creatorStudentId != null && creatorStudentId.equals(student.getStudentId());

        if (!isLeader && !isCreator) {
            throw new UnauthorizedException("Access denied: Only the team leader or the author can " + actionName);
        }
    }
}

