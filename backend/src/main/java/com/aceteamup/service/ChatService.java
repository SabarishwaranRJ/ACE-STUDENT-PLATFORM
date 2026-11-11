package com.aceteamup.service;

import com.aceteamup.dto.workspace.TeamMessageRequest;
import com.aceteamup.dto.workspace.TeamMessageResponse;
import com.aceteamup.dto.workspace.TypingEventDto;
import com.aceteamup.entity.*;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final TeamMessageRepository teamMessageRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(TeamMessageRepository teamMessageRepository,
                       TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       NotificationRepository notificationRepository,
                       WorkspaceSecurityService workspaceSecurityService,
                       SimpMessagingTemplate messagingTemplate) {
        this.teamMessageRepository = teamMessageRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.messagingTemplate = messagingTemplate;
    }

    public TeamMessageResponse sendMessage(String email, Long teamId, TeamMessageRequest req) {
        Student sender = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        TeamMessage.MessageType messageType = TeamMessage.MessageType.TEXT;
        if (req.getMessageType() != null) {
            try {
                messageType = TeamMessage.MessageType.valueOf(req.getMessageType().toUpperCase());
            } catch (Exception ignored) {}
        }

        TeamMessage message = TeamMessage.builder()
                .team(team)
                .sender(sender)
                .messageContent(req.getMessageContent().trim())
                .messageType(messageType)
                .attachmentUrl(req.getAttachmentUrl())
                .attachmentName(req.getAttachmentName())
                .replyToMessageId(req.getReplyToMessageId())
                .build();

        TeamMessage saved = teamMessageRepository.save(message);
        TeamMessageResponse response = mapToResponse(saved);

        // Broadcast to team topic via WebSocket
        String destination = "/topic/team/" + teamId + "/messages";
        messagingTemplate.convertAndSend(destination, response);
        log.info("Broadcasted message {} to {}", saved.getMessageId(), destination);

        // Notify other team members
        notifyTeamMembers(team, sender, saved);

        return response;
    }

    @Transactional(readOnly = true)
    public List<TeamMessageResponse> getMessageHistory(String email, Long teamId) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        return teamMessageRepository.findByTeamTeamIdOrderByCreatedAtAsc(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteMessage(String email, Long teamId, Long messageId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        TeamMessage message = teamMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        boolean isSender = message.getSender().getStudentId().equals(student.getStudentId());
        boolean isLeader = workspaceSecurityService.isTeamLeader(student, team);

        if (!isSender && !isLeader) {
            throw new UnauthorizedException("You are not authorized to delete this message");
        }

        teamMessageRepository.delete(message);

        // Send system deletion notification over WebSocket
        TeamMessageResponse deleteNotice = TeamMessageResponse.builder()
                .messageId(messageId)
                .teamId(teamId)
                .messageType("DELETE")
                .messageContent("Message deleted")
                .build();
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/messages", deleteNotice);
    }

    public void broadcastTyping(String email, Long teamId, boolean isTyping) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TypingEventDto typingEvent = TypingEventDto.builder()
                .teamId(teamId)
                .studentId(student.getStudentId())
                .studentName(student.getName())
                .isTyping(isTyping)
                .build();

        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/typing", typingEvent);
    }

    private void notifyTeamMembers(Team team, Student sender, TeamMessage message) {
        try {
            List<TeamMember> members = teamMemberRepository.findByTeamTeamId(team.getTeamId());
            String preview = message.getMessageContent();
            if (preview.length() > 60) {
                preview = preview.substring(0, 57) + "...";
            }

            for (TeamMember m : members) {
                if (m.getMembershipStatus() == TeamMember.MembershipStatus.Active &&
                        !m.getStudent().getStudentId().equals(sender.getStudentId())) {

                    Notification notif = Notification.builder()
                            .recipient(m.getStudent())
                            .notificationType("MESSAGE")
                            .title("New message in " + team.getTeamName())
                            .message(sender.getName() + ": " + preview)
                            .relatedEntityId(team.getTeamId())
                            .isRead(false)
                            .build();

                    Notification savedNotif = notificationRepository.save(notif);
                    com.aceteamup.dto.workspace.NotificationResponse notifDto = com.aceteamup.dto.workspace.NotificationResponse.builder()
                            .notificationId(savedNotif.getNotificationId())
                            .recipientId(m.getStudent().getStudentId())
                            .notificationType(savedNotif.getNotificationType())
                            .title(savedNotif.getTitle())
                            .message(savedNotif.getMessage())
                            .relatedEntityId(savedNotif.getRelatedEntityId())
                            .isRead(savedNotif.getIsRead())
                            .createdAt(savedNotif.getCreatedAt())
                            .build();
                    messagingTemplate.convertAndSend(
                            "/topic/notifications/" + m.getStudent().getStudentId(),
                            notifDto
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch chat notifications: {}", e.getMessage());
        }
    }

    public TeamMessageResponse mapToResponse(TeamMessage m) {
        return TeamMessageResponse.builder()
                .messageId(m.getMessageId())
                .teamId(m.getTeam().getTeamId())
                .senderId(m.getSender().getStudentId())
                .senderName(m.getSender().getName())
                .senderEmail(m.getSender().getEmail())
                .messageContent(m.getMessageContent())
                .messageType(m.getMessageType() != null ? m.getMessageType().name() : "TEXT")
                .attachmentUrl(m.getAttachmentUrl())
                .attachmentName(m.getAttachmentName())
                .replyToMessageId(m.getReplyToMessageId())
                .createdAt(m.getCreatedAt())
                .build();
    }
}

