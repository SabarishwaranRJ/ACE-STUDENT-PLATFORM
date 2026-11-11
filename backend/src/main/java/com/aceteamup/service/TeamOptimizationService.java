package com.aceteamup.service;

import com.aceteamup.dto.ai.OptimizeTeamRequest;
import com.aceteamup.dto.ai.OptimizeTeamResponse;
import com.aceteamup.dto.ai.SkillGapRequest;
import com.aceteamup.dto.ai.SkillGapResponse;
import com.aceteamup.entity.*;
import com.aceteamup.entity.TeamMember.MembershipStatus;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TeamOptimizationService {

    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRequirementRepository teamRequirementRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentInterestRepository studentInterestRepository;
    private final StudentAvailabilityRepository studentAvailabilityRepository;
    private final AiServiceClient aiServiceClient;

    public TeamOptimizationService(TeamRepository teamRepository,
                                   EventRepository eventRepository,
                                   StudentRepository studentRepository,
                                   TeamMemberRepository teamMemberRepository,
                                   TeamRequirementRepository teamRequirementRepository,
                                   StudentSkillRepository studentSkillRepository,
                                   StudentInterestRepository studentInterestRepository,
                                   StudentAvailabilityRepository studentAvailabilityRepository,
                                   AiServiceClient aiServiceClient) {
        this.teamRepository = teamRepository;
        this.eventRepository = eventRepository;
        this.studentRepository = studentRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRequirementRepository = teamRequirementRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.studentInterestRepository = studentInterestRepository;
        this.studentAvailabilityRepository = studentAvailabilityRepository;
        this.aiServiceClient = aiServiceClient;
    }

    public OptimizeTeamResponse optimizeTeam(OptimizeTeamRequest req) {
        Event event = eventRepository.findById(req.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + req.getEventId()));

        List<Student> candidates;
        if (req.getCandidateStudentIds() != null && !req.getCandidateStudentIds().isEmpty()) {
            candidates = studentRepository.findAllById(req.getCandidateStudentIds());
        } else {
            // All students not in another team for this event
            candidates = studentRepository.findAll().stream()
                    .filter(s -> teamMemberRepository.findByStudentStudentIdAndEventId(s.getStudentId(), req.getEventId()).isEmpty())
                    .filter(s -> s.getProfileVisibility() != Student.ProfileVisibility.Private)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> candidatePayloads = candidates.stream()
                .map(this::buildStudentPayload)
                .collect(Collectors.toList());

        List<String> requiredSkills = req.getRequiredSkills() != null ? req.getRequiredSkills() : Collections.emptyList();
        if (requiredSkills.isEmpty() && req.getTeamId() != null) {
            requiredSkills = teamRequirementRepository.findByTeamTeamId(req.getTeamId()).stream()
                    .map(tr -> tr.getRequiredSkill().getSkillName())
                    .collect(Collectors.toList());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("candidateStudents", candidatePayloads);
        payload.put("requiredSkills", requiredSkills);
        payload.put("minTeamSize", req.getMinTeamSize());
        payload.put("maxTeamSize", req.getMaxTeamSize());
        payload.put("eventDescription", event.getDescription());

        return aiServiceClient.optimizeTeam(payload);
    }

    public SkillGapResponse getSkillGap(Long teamId, Long eventId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        // Get existing skills from team members
        List<TeamMember> members = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(teamId, MembershipStatus.Active);
        Set<String> existingSkills = new HashSet<>();
        List<Long> memberIds = new ArrayList<>();

        for (TeamMember m : members) {
            memberIds.add(m.getStudent().getStudentId());
            studentSkillRepository.findByStudentStudentId(m.getStudent().getStudentId())
                    .forEach(ss -> existingSkills.add(ss.getSkill().getSkillName()));
        }

        // Required event skills from team requirements
        List<String> requiredSkills = teamRequirementRepository.findByTeamTeamId(teamId).stream()
                .map(tr -> tr.getRequiredSkill().getSkillName())
                .collect(Collectors.toList());

        Map<String, Object> teamPayload = new HashMap<>();
        teamPayload.put("teamId", team.getTeamId());
        teamPayload.put("eventId", event.getEventId());
        teamPayload.put("existingMemberIds", memberIds);
        teamPayload.put("existingSkills", new ArrayList<>(existingSkills));
        teamPayload.put("requiredSkills", requiredSkills);
        teamPayload.put("maxSize", event.getRequiredTeamSize() != null ? event.getRequiredTeamSize() : 5);
        teamPayload.put("currentSize", members.size());

        // Eligible candidates not in team
        List<Map<String, Object>> candidatePayloads = studentRepository.findAll().stream()
                .filter(s -> !memberIds.contains(s.getStudentId()))
                .filter(s -> teamMemberRepository.findByStudentStudentIdAndEventId(s.getStudentId(), eventId).isEmpty())
                .filter(s -> s.getProfileVisibility() != Student.ProfileVisibility.Private)
                .map(this::buildStudentPayload)
                .collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("team", teamPayload);
        payload.put("requiredEventSkills", requiredSkills);
        payload.put("candidateStudents", candidatePayloads);

        return aiServiceClient.getSkillGap(payload);
    }

    private Map<String, Object> buildStudentPayload(Student s) {
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", s.getStudentId());
        map.put("name", s.getName());
        map.put("bio", s.getBio());
        map.put("preferredRole", s.getPreferredRole());
        map.put("experienceLevel", s.getExperienceLevel() != null ? s.getExperienceLevel().name() : "Intermediate");
        map.put("availability", s.getAvailability() != null ? s.getAvailability().getDbValue() : "Full-time");

        List<Map<String, Object>> skills = studentSkillRepository.findByStudentStudentId(s.getStudentId()).stream()
                .map(ss -> {
                    Map<String, Object> sm = new HashMap<>();
                    sm.put("skillName", ss.getSkill().getSkillName());
                    sm.put("proficiencyLevel", ss.getProficiencyLevel() != null ? ss.getProficiencyLevel().name() : "Intermediate");
                    return sm;
                })
                .collect(Collectors.toList());
        map.put("skills", skills);
        return map;
    }
}
