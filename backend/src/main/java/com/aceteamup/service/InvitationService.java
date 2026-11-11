package com.aceteamup.service;

import com.aceteamup.dto.invitation.InvitationResponse;
import com.aceteamup.dto.invitation.RespondInvitationRequest;
import com.aceteamup.dto.invitation.SendInvitationRequest;
import com.aceteamup.entity.*;
import com.aceteamup.entity.Invitation.InvitationStatus;
import com.aceteamup.entity.Team.TeamStatus;
import com.aceteamup.entity.TeamMember.MembershipStatus;
import com.aceteamup.entity.TeamMember.TeamMemberId;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.InvitationRepository;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final StudentRepository studentRepository;
    private final TeamService teamService;
    private final StudentService studentService;

    public InvitationService(InvitationRepository invitationRepository,
                             TeamRepository teamRepository,
                             TeamMemberRepository teamMemberRepository,
                             StudentRepository studentRepository,
                             TeamService teamService,
                             StudentService studentService) {
        this.invitationRepository = invitationRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.studentRepository = studentRepository;
        this.teamService = teamService;
        this.studentService = studentService;
    }

    @Transactional
    public InvitationResponse sendInvitation(String senderEmail, SendInvitationRequest req) {
        Student sender = studentRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        Student receiver = studentRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found: " + req.getReceiverId()));
        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + req.getTeamId()));

        if (team.getTeamStatus() != TeamStatus.Open) {
            throw new BadRequestException("Team is not open for invitations");
        }

        // Check sender is leader or active member
        TeamMemberId senderId = new TeamMemberId(team.getTeamId(), sender.getStudentId());
        Optional<TeamMember> senderMembership = teamMemberRepository.findById(senderId);
        if (senderMembership.isEmpty() || senderMembership.get().getMembershipStatus() != MembershipStatus.Active) {
            throw new UnauthorizedException("Only active team members or leaders can send invitations");
        }

        // Check if receiver is already in a team for this event
        if (teamMemberRepository.findByStudentStudentIdAndEventId(receiver.getStudentId(), team.getEvent().getEventId()).isPresent()) {
            throw new BadRequestException("Student is already a member of a team in this event");
        }

        // Handle existing invitation for team and receiver
        Optional<Invitation> existingInv = invitationRepository.findByTeamTeamIdAndReceiverStudentIdAndInvitationStatus(
                team.getTeamId(), receiver.getStudentId(), InvitationStatus.Pending
        );
        if (existingInv.isPresent()) {
            throw new DuplicateResourceException("A pending invitation already exists for this student");
        }

        Invitation invitation = Invitation.builder()
                .team(team)
                .sender(sender)
                .receiver(receiver)
                .invitationStatus(InvitationStatus.Pending)
                .build();

        Invitation saved = invitationRepository.save(invitation);
        return mapToResponse(saved);
    }

    @Transactional
    public InvitationResponse respondToInvitation(String receiverEmail, Long invitationId, RespondInvitationRequest req) {
        Student receiver = studentRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getReceiver().getStudentId().equals(receiver.getStudentId())) {
            throw new UnauthorizedException("You are not the recipient of this invitation");
        }

        if (invitation.getInvitationStatus() != InvitationStatus.Pending) {
            throw new BadRequestException("Invitation is already " + invitation.getInvitationStatus());
        }

        if (req.isAccept()) {
            Team team = invitation.getTeam();
            // Capacity check
            List<TeamMember> active = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(team.getTeamId(), MembershipStatus.Active);
            int maxCapacity = team.getEvent().getRequiredTeamSize() != null ? team.getEvent().getRequiredTeamSize() : 5;
            if (active.size() >= maxCapacity) {
                throw new BadRequestException("Team is already at maximum capacity");
            }

            // Check if student in another team
            if (teamMemberRepository.findByStudentStudentIdAndEventId(receiver.getStudentId(), team.getEvent().getEventId()).isPresent()) {
                throw new BadRequestException("You are already in a team for this event");
            }

            TeamMemberId memberId = new TeamMemberId(team.getTeamId(), receiver.getStudentId());
            TeamMember newMember = TeamMember.builder()
                    .id(memberId)
                    .team(team)
                    .student(receiver)
                    .eventId(team.getEvent().getEventId())
                    .assignedRole(receiver.getPreferredRole() != null ? receiver.getPreferredRole() : "Member")
                    .membershipStatus(MembershipStatus.Active)
                    .build();

            teamMemberRepository.save(newMember);

            if (active.size() + 1 >= maxCapacity) {
                team.setTeamStatus(TeamStatus.Full);
                teamRepository.save(team);
            }

            invitation.setInvitationStatus(InvitationStatus.Accepted);
        } else {
            invitation.setInvitationStatus(InvitationStatus.Declined);
        }

        Invitation updated = invitationRepository.save(invitation);
        return mapToResponse(updated);
    }

    public List<InvitationResponse> getMyInvitations(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return invitationRepository.findByReceiverStudentIdAndInvitationStatus(student.getStudentId(), InvitationStatus.Pending)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InvitationResponse> getSentInvitations(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return invitationRepository.findAll().stream()
                .filter(inv -> inv.getSender().getStudentId().equals(student.getStudentId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void withdrawInvitation(String email, Long invitationId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));

        if (!invitation.getSender().getStudentId().equals(student.getStudentId()) &&
            !invitation.getTeam().getLeader().getStudentId().equals(student.getStudentId())) {
            throw new UnauthorizedException("Only the sender or team leader can withdraw this invitation");
        }

        invitation.setInvitationStatus(InvitationStatus.Withdrawn);
        invitationRepository.save(invitation);
    }

    private InvitationResponse mapToResponse(Invitation inv) {
        int active = teamMemberRepository.findByTeamTeamIdAndMembershipStatus(inv.getTeam().getTeamId(), MembershipStatus.Active).size();
        return InvitationResponse.builder()
                .invitationId(inv.getInvitationId())
                .team(teamService.mapToResponse(inv.getTeam(), active))
                .sender(studentService.mapToSummaryResponse(inv.getSender()))
                .receiver(studentService.mapToSummaryResponse(inv.getReceiver()))
                .invitationStatus(inv.getInvitationStatus().name())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
