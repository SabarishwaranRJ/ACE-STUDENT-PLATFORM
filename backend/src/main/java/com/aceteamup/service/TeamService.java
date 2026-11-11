package com.aceteamup.service;

import com.aceteamup.dto.event.EventResponse;
import com.aceteamup.dto.skill.SkillResponse;
import com.aceteamup.dto.student.StudentSummaryResponse;
import com.aceteamup.dto.team.*;
import com.aceteamup.entity.*;
import com.aceteamup.entity.StudentSkill.ProficiencyLevel;
import com.aceteamup.entity.Team.TeamStatus;
import com.aceteamup.entity.TeamMember.MembershipStatus;
import com.aceteamup.entity.TeamMember.TeamMemberId;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final TeamRequirementRepository teamRequirementRepository;
    private final StudentService studentService;
    private final EventService eventService;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       EventRepository eventRepository,
                       StudentRepository studentRepository,
                       SkillRepository skillRepository,
                       TeamRequirementRepository teamRequirementRepository,
                       StudentService studentService,
                       EventService eventService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
        this.teamRequirementRepository = teamRequirementRepository;
        this.studentService = studentService;
        this.eventService = eventService;
    }

    @Transactional
    public TeamResponse createTeam(String email, CreateTeamRequest req) {
        Student leader = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + req.getEventId()));

        // Check if student already in a team for this event
        if (teamMemberRepository.findByStudentStudentIdAndEventId(leader.getStudentId(), event.getEventId()).isPresent()) {
            throw new DuplicateResourceException("You are already a member of a team for this event");
        }

        Team team = Team.builder()
                .teamName(req.getTeamName())
                .event(event)
                .leader(leader)
                .description(req.getDescription())
                .teamStatus(TeamStatus.Open)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add leader as Active member
        TeamMemberId memberId = new TeamMemberId(savedTeam.getTeamId(), leader.getStudentId());
        TeamMember leaderMember = TeamMember.builder()
                .id(memberId)
                .team(savedTeam)
                .student(leader)
                .eventId(event.getEventId())
                .assignedRole("Team Leader")
                .membershipStatus(MembershipStatus.Active)
                .build();

        teamMemberRepository.save(leaderMember);

        return mapToResponse(savedTeam, 1);
    }

    public TeamDetailResponse getTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        List<TeamMember> members = teamMemberRepository.findByTeamTeamId(teamId);
        List<TeamRequirement> requirements = teamRequirementRepository.findByTeamTeamId(teamId);

        List<TeamMemberResponse> memberResponses = members.stream()
                .map(m -> TeamMemberResponse.builder()
                        .studentId(m.getStudent().getStudentId())
                        .name(m.getStudent().getName())
                        .email(m.getStudent().getEmail())
                        .assignedRole(m.getAssignedRole())
                        .membershipStatus(m.getMembershipStatus().name())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        List<TeamRequirementResponse> reqResponses = requirements.stream()
                .map(r -> TeamRequirementResponse.builder()
                        .requirementId(r.getRequirementId())
                        .skill(SkillResponse.builder()
                                .skillId(r.getRequiredSkill().getSkillId())
                                .skillName(r.getRequiredSkill().getSkillName())
                                .skillCategory(r.getRequiredSkill().getSkillCategory())
                                .build())
                        .minimumProficiency(r.getMinimumProficiency() != null ? r.getMinimumProficiency().name() : null)
                        .build())
                .collect(Collectors.toList());

        int activeCount = (int) members.stream()
                .filter(m -> m.getMembershipStatus() == MembershipStatus.Active)
                .count();

        return TeamDetailResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .event(eventService.mapToResponse(team.getEvent()))
                .leader(studentService.mapToSummaryResponse(team.getLeader()))
                .description(team.getDescription())
                .teamStatus(team.getTeamStatus().name())
                .memberCount(activeCount)
                .createdAt(team.getCreatedAt())
                .members(memberResponses)
                .requirements(reqResponses)
                .build();
    }

    public List<TeamResponse> getTeamsForEvent(Long eventId, String status) {
        List<Team> teams;
        if (status != null && !status.isBlank()) {
            try {
                TeamStatus teamStatus = TeamStatus.valueOf(status);
                teams = teamRepository.findByEventEventIdAndTeamStatus(eventId, teamStatus);
            } catch (Exception e) {
                teams = teamRepository.findByEventEventId(eventId);
            }
        } else {
            teams = teamRepository.findByEventEventId(eventId);
        }

        return teams.stream()
                .map(t -> {
                    int active = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(t.getTeamId(), MembershipStatus.Active).size();
                    return mapToResponse(t, active);
                })
                .collect(Collectors.toList());
    }

    public List<TeamResponse> getMyTeams(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        List<TeamMember> memberships = teamMemberRepository.findByStudentStudentId(student.getStudentId());

        return memberships.stream()
                .filter(m -> m.getMembershipStatus() == MembershipStatus.Active)
                .map(m -> {
                    Team t = m.getTeam();
                    int active = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(t.getTeamId(), MembershipStatus.Active).size();
                    return mapToResponse(t, active);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamResponse updateTeam(String email, Long teamId, UpdateTeamRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (!team.getLeader().getStudentId().equals(student.getStudentId())) {
            throw new UnauthorizedException("Only the team leader can update team details");
        }

        if (req.getTeamName() != null) team.setTeamName(req.getTeamName());
        if (req.getDescription() != null) team.setDescription(req.getDescription());
        if (req.getTeamStatus() != null) {
            try {
                team.setTeamStatus(TeamStatus.valueOf(req.getTeamStatus()));
            } catch (Exception ignored) {}
        }

        Team updated = teamRepository.save(team);
        int active = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(teamId, MembershipStatus.Active).size();
        return mapToResponse(updated, active);
    }

    @Transactional
    public TeamMemberResponse requestToJoin(String email, JoinTeamRequest req) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + req.getTeamId()));

        if (team.getTeamStatus() != TeamStatus.Open) {
            throw new BadRequestException("Team is not open to new members");
        }

        // Check if student already in team for this event
        if (teamMemberRepository.findByStudentStudentIdAndEventId(student.getStudentId(), team.getEvent().getEventId()).isPresent()) {
            throw new DuplicateResourceException("You are already a member or applicant for a team in this event");
        }

        TeamMemberId id = new TeamMemberId(team.getTeamId(), student.getStudentId());
        TeamMember member = TeamMember.builder()
                .id(id)
                .team(team)
                .student(student)
                .eventId(team.getEvent().getEventId())
                .assignedRole(req.getAssignedRole() != null ? req.getAssignedRole() : "Applicant")
                .membershipStatus(MembershipStatus.Pending)
                .build();

        TeamMember saved = teamMemberRepository.save(member);
        return TeamMemberResponse.builder()
                .studentId(saved.getStudent().getStudentId())
                .name(saved.getStudent().getName())
                .email(saved.getStudent().getEmail())
                .assignedRole(saved.getAssignedRole())
                .membershipStatus(saved.getMembershipStatus().name())
                .joinedAt(saved.getJoinedAt())
                .build();
    }

    @Transactional
    public void approveMember(String email, Long teamId, Long studentId) {
        Student leader = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (!team.getLeader().getStudentId().equals(leader.getStudentId())) {
            throw new UnauthorizedException("Only the team leader can approve members");
        }

        TeamMemberId id = new TeamMemberId(teamId, studentId);
        TeamMember member = teamMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership request not found"));

        // Capacity check
        List<TeamMember> activeMembers = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(teamId, MembershipStatus.Active);
        int maxCapacity = team.getEvent().getRequiredTeamSize() != null ? team.getEvent().getRequiredTeamSize() : 5;
        if (activeMembers.size() >= maxCapacity) {
            throw new BadRequestException("Team has reached its maximum size of " + maxCapacity);
        }

        member.setMembershipStatus(MembershipStatus.Active);
        teamMemberRepository.save(member);

        if (activeMembers.size() + 1 >= maxCapacity) {
            team.setTeamStatus(TeamStatus.Full);
            teamRepository.save(team);
        }
    }

    @Transactional
    public void removeMember(String email, Long teamId, Long studentId) {
        Student leader = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (!team.getLeader().getStudentId().equals(leader.getStudentId())) {
            throw new UnauthorizedException("Only the team leader can remove members");
        }

        if (leader.getStudentId().equals(studentId)) {
            throw new BadRequestException("Leader cannot be removed; please disband the team instead");
        }

        TeamMemberId id = new TeamMemberId(teamId, studentId);
        teamMemberRepository.deleteById(id);

        if (team.getTeamStatus() == TeamStatus.Full) {
            team.setTeamStatus(TeamStatus.Open);
            teamRepository.save(team);
        }
    }

    @Transactional
    public void leaveTeam(String email, Long teamId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (team.getLeader().getStudentId().equals(student.getStudentId())) {
            throw new BadRequestException("Leader cannot leave the team. Transfer leadership or disband the team.");
        }

        TeamMemberId id = new TeamMemberId(teamId, student.getStudentId());
        teamMemberRepository.deleteById(id);

        if (team.getTeamStatus() == TeamStatus.Full) {
            team.setTeamStatus(TeamStatus.Open);
            teamRepository.save(team);
        }
    }

    // Requirements
    @Transactional
    public TeamRequirementResponse addTeamRequirement(String email, Long teamId, TeamRequirementRequest req) {
        Student leader = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (!team.getLeader().getStudentId().equals(leader.getStudentId())) {
            throw new UnauthorizedException("Only the team leader can manage team requirements");
        }

        Skill skill = skillRepository.findById(req.getRequiredSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + req.getRequiredSkillId()));

        TeamRequirement.MinimumProficiency minProf = TeamRequirement.MinimumProficiency.Beginner;
        if (req.getMinimumProficiency() != null) {
            try {
                minProf = TeamRequirement.MinimumProficiency.valueOf(req.getMinimumProficiency());
            } catch (Exception ignored) {}
        }

        TeamRequirement tr = teamRequirementRepository
                .findByTeamTeamIdAndRequiredSkillSkillId(teamId, skill.getSkillId())
                .orElse(TeamRequirement.builder().team(team).requiredSkill(skill).build());

        tr.setMinimumProficiency(minProf);
        TeamRequirement saved = teamRequirementRepository.save(tr);

        return TeamRequirementResponse.builder()
                .requirementId(saved.getRequirementId())
                .skill(SkillResponse.builder()
                        .skillId(skill.getSkillId())
                        .skillName(skill.getSkillName())
                        .skillCategory(skill.getSkillCategory())
                        .build())
                .minimumProficiency(saved.getMinimumProficiency().name())
                .build();
    }

    @Transactional
    public void removeTeamRequirement(String email, Long teamId, Long reqId) {
        Student leader = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (!team.getLeader().getStudentId().equals(leader.getStudentId())) {
            throw new UnauthorizedException("Only the team leader can manage team requirements");
        }

        teamRequirementRepository.deleteById(reqId);
    }

    public List<TeamRequirementResponse> getTeamRequirements(Long teamId) {
        return teamRequirementRepository.findByTeamTeamId(teamId).stream()
                .map(r -> TeamRequirementResponse.builder()
                        .requirementId(r.getRequirementId())
                        .skill(SkillResponse.builder()
                                .skillId(r.getRequiredSkill().getSkillId())
                                .skillName(r.getRequiredSkill().getSkillName())
                                .skillCategory(r.getRequiredSkill().getSkillCategory())
                                .build())
                        .minimumProficiency(r.getMinimumProficiency() != null ? r.getMinimumProficiency().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public TeamResponse mapToResponse(Team t, int memberCount) {
        return TeamResponse.builder()
                .teamId(t.getTeamId())
                .teamName(t.getTeamName())
                .event(eventService.mapToResponse(t.getEvent()))
                .leader(studentService.mapToSummaryResponse(t.getLeader()))
                .description(t.getDescription())
                .teamStatus(t.getTeamStatus().name())
                .memberCount(memberCount)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
