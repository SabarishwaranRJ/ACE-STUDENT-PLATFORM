package com.aceteamup.service;

import com.aceteamup.dto.team.CreateTeamRequest;
import com.aceteamup.dto.team.TeamResponse;
import com.aceteamup.entity.Event;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamMember;
import com.aceteamup.exception.DuplicateResourceException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.*;
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
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private TeamRequirementRepository teamRequirementRepository;
    @Mock
    private StudentService studentService;
    @Mock
    private EventService eventService;

    @InjectMocks
    private TeamService teamService;

    private Student student;
    private Event event;
    private Team team;

    @BeforeEach
    void setUp() {
        student = Student.builder().studentId(1L).name("Alice").email("alice@example.com").build();
        event = Event.builder().eventId(10L).eventName("Hackathon 2026").requiredTeamSize(4).build();
        team = Team.builder().teamId(100L).teamName("CodeCrafters").event(event).leader(student).build();
    }

    @Test
    void testCreateTeamSuccess() {
        CreateTeamRequest req = CreateTeamRequest.builder().teamName("CodeCrafters").eventId(10L).description("Hack team").build();

        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(teamMemberRepository.findByStudentStudentIdAndEventId(1L, 10L)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamResponse res = teamService.createTeam("alice@example.com", req);

        assertNotNull(res);
        assertEquals("CodeCrafters", res.getTeamName());
        verify(teamMemberRepository, times(1)).save(any(TeamMember.class));
    }

    @Test
    void testCreateTeamDuplicateEventMembershipThrows() {
        CreateTeamRequest req = CreateTeamRequest.builder().teamName("CodeCrafters").eventId(10L).build();

        when(studentRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(teamMemberRepository.findByStudentStudentIdAndEventId(1L, 10L)).thenReturn(Optional.of(mock(TeamMember.class)));

        assertThrows(DuplicateResourceException.class, () -> teamService.createTeam("alice@example.com", req));
        verify(teamRepository, never()).save(any(Team.class));
    }
}
