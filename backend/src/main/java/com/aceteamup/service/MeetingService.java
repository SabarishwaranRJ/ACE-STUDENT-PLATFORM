package com.aceteamup.service;

import com.aceteamup.dto.workspace.TeamMeetingRequest;
import com.aceteamup.dto.workspace.TeamMeetingResponse;
import com.aceteamup.entity.Notification;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamMeeting;
import com.aceteamup.entity.TeamMember;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.NotificationRepository;
import com.aceteamup.repository.TeamMeetingRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MeetingService {

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private final TeamMeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final SimpMessagingTemplate messagingTemplate;

    public MeetingService(TeamMeetingRepository meetingRepository,
                          TeamRepository teamRepository,
                          TeamMemberRepository teamMemberRepository,
                          NotificationRepository notificationRepository,
                          WorkspaceSecurityService workspaceSecurityService,
                          SimpMessagingTemplate messagingTemplate) {
        this.meetingRepository = meetingRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<TeamMeetingResponse> getTeamMeetings(String email, Long teamId) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        return meetingRepository.findByTeamTeamIdOrderByStartTimeDesc(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeamMeetingResponse scheduleMeeting(String email, Long teamId, TeamMeetingRequest req) {
        Student creator = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (req.getEndTime() != null && req.getEndTime().isBefore(req.getStartTime())) {
            throw new BadRequestException("Meeting end time cannot be before start time");
        }

        TeamMeeting meeting = TeamMeeting.builder()
                .team(team)
                .createdBy(creator)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .meetingLink(req.getMeetingLink().trim())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .build();

        TeamMeeting saved = meetingRepository.save(meeting);
        TeamMeetingResponse response = mapToResponse(saved);

        // Broadcast to team topic
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/meetings", response);

        // Notify teammates
        notifyTeammates(team, creator, saved);

        return response;
    }

    public void cancelMeeting(String email, Long teamId, Long meetingId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamMeeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + meetingId));

        if (!meeting.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Meeting does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, meeting.getTeam(), meeting.getCreatedBy().getStudentId(), "cancel this meeting");

        meetingRepository.delete(meeting);

        TeamMeetingResponse cancelNotice = TeamMeetingResponse.builder()
                .meetingId(meetingId)
                .teamId(teamId)
                .title("CANCELLED")
                .build();
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/meetings", cancelNotice);
    }

    private void notifyTeammates(Team team, Student creator, TeamMeeting meeting) {
        try {
            List<TeamMember> members = teamMemberRepository.findByTeamTeamId(team.getTeamId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            String formattedTime = meeting.getStartTime().format(formatter);

            for (TeamMember m : members) {
                if (m.getMembershipStatus() == TeamMember.MembershipStatus.Active &&
                        !m.getStudent().getStudentId().equals(creator.getStudentId())) {

                    Notification notif = Notification.builder()
                            .recipient(m.getStudent())
                            .notificationType("MEETING")
                            .title("Meeting Scheduled: " + meeting.getTitle())
                            .message("Scheduled by " + creator.getName() + " for " + formattedTime)
                            .relatedEntityId(meeting.getMeetingId())
                            .isRead(false)
                            .build();

                    Notification saved = notificationRepository.save(notif);
                    com.aceteamup.dto.workspace.NotificationResponse dto = com.aceteamup.dto.workspace.NotificationResponse.builder()
                            .notificationId(saved.getNotificationId())
                            .recipientId(m.getStudent().getStudentId())
                            .notificationType(saved.getNotificationType())
                            .title(saved.getTitle())
                            .message(saved.getMessage())
                            .relatedEntityId(saved.getRelatedEntityId())
                            .isRead(saved.getIsRead())
                            .createdAt(saved.getCreatedAt())
                            .build();
                    messagingTemplate.convertAndSend("/topic/notifications/" + m.getStudent().getStudentId(), dto);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to notify teammates for meeting: {}", e.getMessage());
        }
    }

    public TeamMeetingResponse mapToResponse(TeamMeeting m) {
        return TeamMeetingResponse.builder()
                .meetingId(m.getMeetingId())
                .teamId(m.getTeam().getTeamId())
                .title(m.getTitle())
                .description(m.getDescription())
                .meetingLink(m.getMeetingLink())
                .startTime(m.getStartTime())
                .endTime(m.getEndTime())
                .createdById(m.getCreatedBy().getStudentId())
                .createdByName(m.getCreatedBy().getName())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

