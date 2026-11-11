package com.aceteamup.service;

import com.aceteamup.dto.student.StudentSummaryResponse;
import com.aceteamup.dto.workspace.ConversationResponse;
import com.aceteamup.dto.workspace.DirectMessageRequest;
import com.aceteamup.dto.workspace.DirectMessageResponse;
import com.aceteamup.entity.Conversation;
import com.aceteamup.entity.ConversationMember;
import com.aceteamup.entity.DirectMessage;
import com.aceteamup.entity.Notification;
import com.aceteamup.entity.Student;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.exception.UnauthorizedException;
import com.aceteamup.repository.ConversationMemberRepository;
import com.aceteamup.repository.ConversationRepository;
import com.aceteamup.repository.DirectMessageRepository;
import com.aceteamup.repository.NotificationRepository;
import com.aceteamup.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DirectMessageService {

    private static final Logger log = LoggerFactory.getLogger(DirectMessageService.class);

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final DirectMessageRepository directMessageRepository;
    private final StudentRepository studentRepository;
    private final NotificationRepository notificationRepository;
    private final StudentService studentService;
    private final SimpMessagingTemplate messagingTemplate;

    public DirectMessageService(ConversationRepository conversationRepository,
                                ConversationMemberRepository conversationMemberRepository,
                                DirectMessageRepository directMessageRepository,
                                StudentRepository studentRepository,
                                NotificationRepository notificationRepository,
                                StudentService studentService,
                                SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.directMessageRepository = directMessageRepository;
        this.studentRepository = studentRepository;
        this.notificationRepository = notificationRepository;
        this.studentService = studentService;
        this.messagingTemplate = messagingTemplate;
    }

    public ConversationResponse getOrCreateConversation(String email, Long targetStudentId) {
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        if (currentStudent.getStudentId().equals(targetStudentId)) {
            throw new BadRequestException("You cannot start a direct message conversation with yourself");
        }

        Student targetStudent = studentRepository.findById(targetStudentId)
                .orElseThrow(() -> new ResourceNotFoundException("Target student not found: " + targetStudentId));

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository
                .findConversationBetweenStudents(currentStudent.getStudentId(), targetStudent.getStudentId());

        Conversation conversation;
        if (existing.isPresent()) {
            conversation = existing.get();
        } else {
            conversation = conversationRepository.save(new Conversation());

            ConversationMember m1 = ConversationMember.builder()
                    .id(new ConversationMember.ConversationMemberId(conversation.getConversationId(), currentStudent.getStudentId()))
                    .conversation(conversation)
                    .student(currentStudent)
                    .build();

            ConversationMember m2 = ConversationMember.builder()
                    .id(new ConversationMember.ConversationMemberId(conversation.getConversationId(), targetStudent.getStudentId()))
                    .conversation(conversation)
                    .student(targetStudent)
                    .build();

            conversationMemberRepository.save(m1);
            conversationMemberRepository.save(m2);
        }

        return buildConversationResponse(conversation, currentStudent);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(String email) {
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        List<ConversationMember> memberships = conversationMemberRepository.findByStudentStudentId(currentStudent.getStudentId());
        List<ConversationResponse> results = new ArrayList<>();

        for (ConversationMember cm : memberships) {
            Conversation conv = cm.getConversation();
            results.add(buildConversationResponse(conv, currentStudent));
        }

        // Sort by last message / updated at descending
        results.sort(Comparator.comparing(
                (ConversationResponse c) -> c.getLastMessage() != null ? c.getLastMessage().getCreatedAt() : c.getUpdatedAt(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        return results;
    }

    public List<DirectMessageResponse> getConversationMessages(String email, Long conversationId) {
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        validateMembership(conversationId, currentStudent.getStudentId());

        // Mark incoming messages as read
        directMessageRepository.markAllAsRead(conversationId, currentStudent.getStudentId());

        return directMessageRepository.findByConversationConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DirectMessageResponse sendDirectMessage(String email, Long conversationId, DirectMessageRequest req) {
        Student sender = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        validateMembership(conversationId, sender.getStudentId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        DirectMessage dm = DirectMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .messageContent(req.getMessageContent().trim())
                .attachmentUrl(req.getAttachmentUrl())
                .attachmentName(req.getAttachmentName())
                .isRead(false)
                .build();

        DirectMessage saved = directMessageRepository.save(dm);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        DirectMessageResponse response = mapToResponse(saved);

        // Broadcast to conversation topic
        String destination = "/topic/conversation/" + conversationId;
        messagingTemplate.convertAndSend(destination, response);

        // Find recipient to dispatch notification
        conversationMemberRepository.findByConversationConversationIdAndStudentStudentIdNot(conversationId, sender.getStudentId())
                .ifPresent(cm -> {
                    Student recipient = cm.getStudent();
                    String preview = saved.getMessageContent();
                    if (preview.length() > 60) preview = preview.substring(0, 57) + "...";

                    Notification notif = Notification.builder()
                            .recipient(recipient)
                            .notificationType("DIRECT_MESSAGE")
                            .title("Direct message from " + sender.getName())
                            .message(preview)
                            .relatedEntityId(conversationId)
                            .isRead(false)
                            .build();

                    Notification savedNotif = notificationRepository.save(notif);
                    com.aceteamup.dto.workspace.NotificationResponse notifDto = com.aceteamup.dto.workspace.NotificationResponse.builder()
                            .notificationId(savedNotif.getNotificationId())
                            .recipientId(recipient.getStudentId())
                            .notificationType(savedNotif.getNotificationType())
                            .title(savedNotif.getTitle())
                            .message(savedNotif.getMessage())
                            .relatedEntityId(savedNotif.getRelatedEntityId())
                            .isRead(savedNotif.getIsRead())
                            .createdAt(savedNotif.getCreatedAt())
                            .build();
                    messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getStudentId(), notifDto);
                });

        return response;
    }

    public void markAsRead(String email, Long conversationId) {
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));

        validateMembership(conversationId, currentStudent.getStudentId());
        directMessageRepository.markAllAsRead(conversationId, currentStudent.getStudentId());
    }

    public void validateMembership(Long conversationId, Long studentId) {
        boolean isMember = conversationMemberRepository
                .existsByConversationConversationIdAndStudentStudentId(conversationId, studentId);
        if (!isMember) {
            throw new UnauthorizedException("Access denied: You are not a participant in this conversation");
        }
    }

    private ConversationResponse buildConversationResponse(Conversation conv, Student currentStudent) {
        Optional<ConversationMember> otherMemberOpt = conversationMemberRepository
                .findByConversationConversationIdAndStudentStudentIdNot(conv.getConversationId(), currentStudent.getStudentId());

        StudentSummaryResponse otherParticipant = otherMemberOpt
                .map(cm -> studentService.mapToSummaryResponse(cm.getStudent()))
                .orElse(null);

        var latestPage = directMessageRepository.findByConversationConversationIdOrderByCreatedAtDesc(
                conv.getConversationId(), PageRequest.of(0, 1));
        DirectMessageResponse lastMessage = latestPage.hasContent()
                ? mapToResponse(latestPage.getContent().get(0))
                : null;

        long unread = directMessageRepository.countUnreadMessages(conv.getConversationId(), currentStudent.getStudentId());

        return ConversationResponse.builder()
                .conversationId(conv.getConversationId())
                .otherParticipant(otherParticipant)
                .lastMessage(lastMessage)
                .unreadCount(unread)
                .updatedAt(conv.getUpdatedAt())
                .build();
    }

    public DirectMessageResponse mapToResponse(DirectMessage dm) {
        return DirectMessageResponse.builder()
                .messageId(dm.getMessageId())
                .conversationId(dm.getConversation().getConversationId())
                .senderId(dm.getSender().getStudentId())
                .senderName(dm.getSender().getName())
                .messageContent(dm.getMessageContent())
                .attachmentUrl(dm.getAttachmentUrl())
                .attachmentName(dm.getAttachmentName())
                .isRead(dm.getIsRead())
                .createdAt(dm.getCreatedAt())
                .build();
    }
}

