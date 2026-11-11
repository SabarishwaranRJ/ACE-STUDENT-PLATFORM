package com.aceteamup.service;

import com.aceteamup.dto.workspace.TeamAnnouncementRequest;
import com.aceteamup.dto.workspace.TeamAnnouncementResponse;
import com.aceteamup.entity.Notification;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamAnnouncement;
import com.aceteamup.entity.TeamMember;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.NotificationRepository;
import com.aceteamup.repository.TeamAnnouncementRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private final TeamAnnouncementRepository announcementRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final SimpMessagingTemplate messagingTemplate;

    public AnnouncementService(TeamAnnouncementRepository announcementRepository,
                               TeamRepository teamRepository,
                               TeamMemberRepository teamMemberRepository,
                               NotificationRepository notificationRepository,
                               WorkspaceSecurityService workspaceSecurityService,
                               SimpMessagingTemplate messagingTemplate) {
        this.announcementRepository = announcementRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<TeamAnnouncementResponse> getTeamAnnouncements(String email, Long teamId) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        return announcementRepository.findByTeamTeamIdOrderByIsPinnedDescCreatedAtDesc(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeamAnnouncementResponse createAnnouncement(String email, Long teamId, TeamAnnouncementRequest req) {
        Student creator = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        TeamAnnouncement announcement = TeamAnnouncement.builder()
                .team(team)
                .createdBy(creator)
                .title(req.getTitle().trim())
                .content(req.getContent().trim())
                .isPinned(Boolean.TRUE.equals(req.getIsPinned()))
                .build();

        TeamAnnouncement saved = announcementRepository.save(announcement);
        TeamAnnouncementResponse response = mapToResponse(saved);

        // Broadcast to team topic
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/announcements", response);

        // Notify all team members
        notifyTeammates(team, creator, saved);

        return response;
    }

    public TeamAnnouncementResponse updateAnnouncement(String email, Long teamId, Long announcementId, TeamAnnouncementRequest req) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + announcementId));

        if (!announcement.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Announcement does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, announcement.getTeam(), announcement.getCreatedBy().getStudentId(), "update this announcement");

        announcement.setTitle(req.getTitle().trim());
        announcement.setContent(req.getContent().trim());
        if (req.getIsPinned() != null) {
            announcement.setIsPinned(req.getIsPinned());
        }

        TeamAnnouncement saved = announcementRepository.save(announcement);
        TeamAnnouncementResponse response = mapToResponse(saved);

        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/announcements", response);
        return response;
    }

    public TeamAnnouncementResponse togglePin(String email, Long teamId, Long announcementId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + announcementId));

        if (!announcement.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Announcement does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, announcement.getTeam(), announcement.getCreatedBy().getStudentId(), "pin/unpin this announcement");

        announcement.setIsPinned(!Boolean.TRUE.equals(announcement.getIsPinned()));
        TeamAnnouncement saved = announcementRepository.save(announcement);
        TeamAnnouncementResponse response = mapToResponse(saved);

        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/announcements", response);
        return response;
    }

    public void deleteAnnouncement(String email, Long teamId, Long announcementId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamAnnouncement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + announcementId));

        if (!announcement.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Announcement does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, announcement.getTeam(), announcement.getCreatedBy().getStudentId(), "delete this announcement");

        announcementRepository.delete(announcement);

        TeamAnnouncementResponse deleteNotice = TeamAnnouncementResponse.builder()
                .announcementId(announcementId)
                .teamId(teamId)
                .title("DELETED")
                .build();
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/announcements", deleteNotice);
    }

    private void notifyTeammates(Team team, Student creator, TeamAnnouncement announcement) {
        try {
            List<TeamMember> members = teamMemberRepository.findByTeamTeamId(team.getTeamId());
            for (TeamMember m : members) {
                if (m.getMembershipStatus() == TeamMember.MembershipStatus.Active &&
                        !m.getStudent().getStudentId().equals(creator.getStudentId())) {

                    Notification notif = Notification.builder()
                            .recipient(m.getStudent())
                            .notificationType("ANNOUNCEMENT")
                            .title("Announcement in " + team.getTeamName())
                            .message(announcement.getTitle())
                            .relatedEntityId(announcement.getAnnouncementId())
                            .isRead(false)
                            .build();

                    Notification saved = notificationRepository.save(notif);
                    TeamAnnouncementResponse notifDto = TeamAnnouncementResponse.builder()
                            .announcementId(saved.getNotificationId())
                            .teamId(team.getTeamId())
                            .title(notif.getTitle())
                            .content(notif.getMessage())
                            .build();
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
            log.warn("Failed to notify teammates for announcement: {}", e.getMessage());
        }
    }

    public TeamAnnouncementResponse mapToResponse(TeamAnnouncement a) {
        return TeamAnnouncementResponse.builder()
                .announcementId(a.getAnnouncementId())
                .teamId(a.getTeam().getTeamId())
                .title(a.getTitle())
                .content(a.getContent())
                .isPinned(a.getIsPinned())
                .createdById(a.getCreatedBy().getStudentId())
                .createdByName(a.getCreatedBy().getName())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

