package com.aceteamup.service;

import com.aceteamup.dto.invitation.InvitationResponse;
import com.aceteamup.dto.invitation.SendInvitationRequest;
import com.aceteamup.entity.*;
import com.aceteamup.entity.TeamMember.MembershipStatus;
import com.aceteamup.entity.TeamMember.TeamMemberId;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.repository.InvitationRepository;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeamService teamService;
    @Mock
    private StudentService studentService;

    @InjectMocks
    private InvitationService invitationService;

    private Student sender;
    private Student receiver;
    private Event event;
    private Team team;

    @BeforeEach
    void setUp() {
        sender = Student.builder().studentId(1L).name("Alice").email("alice@example.com").build();
        receiver = Student.builder().studentId(2L).name("Bob").email("bob@example.com").build();
        event = Event.builder().eventId(10L).eventName("Hackathon 2026").build();
        team = Team.builder().teamId(100L).teamName("CodeCrafters").event(event).leader(sender).teamStatus(Team.TeamStatus.Open).build();
    }

    @Test
    void testSendInvitationSuccess() {
        SendInvitationRequest req = SendInvitationRequest.builder().teamId(100L).receiverId(2L).build();

        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sender));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));

        TeamMember senderMember = TeamMember.builder().membershipStatus(MembershipStatus.Active).build();
        when(teamMemberRepository.findById(new TeamMemberId(100L, 1L))).thenReturn(Optional.of(senderMember));
        when(teamMemberRepository.findByStudentStudentIdAndEventId(2L, 10L)).thenReturn(Optional.empty());

        Invitation invitation = Invitation.builder().invitationId(1L).team(team).sender(sender).receiver(receiver).invitationStatus(Invitation.InvitationStatus.Pending).build();
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);

        InvitationResponse res = invitationService.sendInvitation("alice@example.com", req);
        assertNotNull(res);
        assertEquals("Pending", res.getInvitationStatus());
    }

    @Test
    void testSendInvitationFailsWhenReceiverAlreadyInEventTeam() {
        SendInvitationRequest req = SendInvitationRequest.builder().teamId(100L).receiverId(2L).build();

        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sender));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(team));

        TeamMember senderMember = TeamMember.builder().membershipStatus(MembershipStatus.Active).build();
        when(teamMemberRepository.findById(new TeamMemberId(100L, 1L))).thenReturn(Optional.of(senderMember));
        when(teamMemberRepository.findByStudentStudentIdAndEventId(2L, 10L)).thenReturn(Optional.of(mock(TeamMember.class)));

        assertThrows(BadRequestException.class, () -> invitationService.sendInvitation("alice@example.com", req));
    }
}
