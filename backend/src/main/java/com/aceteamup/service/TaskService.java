package com.aceteamup.service;

import com.aceteamup.dto.workspace.TeamTaskRequest;
import com.aceteamup.dto.workspace.TeamTaskResponse;
import com.aceteamup.dto.workspace.UpdateTaskStatusRequest;
import com.aceteamup.entity.Notification;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamTask;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.NotificationRepository;
import com.aceteamup.repository.StudentRepository;
import com.aceteamup.repository.TeamRepository;
import com.aceteamup.repository.TeamTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TeamTaskRepository teamTaskRepository;
    private final TeamRepository teamRepository;
    private final StudentRepository studentRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(TeamTaskRepository teamTaskRepository,
                       TeamRepository teamRepository,
                       StudentRepository studentRepository,
                       NotificationRepository notificationRepository,
                       WorkspaceSecurityService workspaceSecurityService,
                       SimpMessagingTemplate messagingTemplate) {
        this.teamTaskRepository = teamTaskRepository;
        this.teamRepository = teamRepository;
        this.studentRepository = studentRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<TeamTaskResponse> getTeamTasks(String email, Long teamId) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        return teamTaskRepository.findByTeamTeamIdOrderByCreatedAtDesc(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeamTaskResponse createTask(String email, Long teamId, TeamTaskRequest req) {
        Student creator = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        Student assignee = null;
        if (req.getAssignedToId() != null) {
            assignee = studentRepository.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee student not found: " + req.getAssignedToId()));

            if (!workspaceSecurityService.isStudentMemberOfTeam(assignee.getStudentId(), teamId)) {
                throw new BadRequestException("Assigned student is not an active member of this team");
            }
        }

        TeamTask.TaskPriority priority = TeamTask.TaskPriority.MEDIUM;
        if (req.getPriority() != null) {
            try {
                priority = TeamTask.TaskPriority.valueOf(req.getPriority().toUpperCase());
            } catch (Exception ignored) {}
        }

        TeamTask.TaskStatus status = TeamTask.TaskStatus.TODO;
        if (req.getStatus() != null) {
            try {
                status = TeamTask.TaskStatus.valueOf(req.getStatus().toUpperCase());
            } catch (Exception ignored) {}
        }

        TeamTask task = TeamTask.builder()
                .team(team)
                .createdBy(creator)
                .assignedTo(assignee)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .priority(priority)
                .status(status)
                .dueDate(req.getDueDate())
                .build();

        TeamTask saved = teamTaskRepository.save(task);
        TeamTaskResponse response = mapToResponse(saved);

        // Broadcast task creation to team members
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/tasks", response);

        // Notify assignee if assigned
        if (assignee != null && !assignee.getStudentId().equals(creator.getStudentId())) {
            notifyAssignee(assignee, team, saved, creator);
        }

        return response;
    }

    public TeamTaskResponse updateTask(String email, Long teamId, Long taskId, TeamTaskRequest req) {
        Student currentStudent = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Task does not belong to team: " + teamId);
        }

        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            task.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getDueDate() != null) {
            task.setDueDate(req.getDueDate());
        }
        if (req.getPriority() != null) {
            try {
                task.setPriority(TeamTask.TaskPriority.valueOf(req.getPriority().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (req.getStatus() != null) {
            try {
                task.setStatus(TeamTask.TaskStatus.valueOf(req.getStatus().toUpperCase()));
            } catch (Exception ignored) {}
        }

        Student oldAssignee = task.getAssignedTo();
        if (req.getAssignedToId() != null) {
            Student newAssignee = studentRepository.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee student not found: " + req.getAssignedToId()));

            if (!workspaceSecurityService.isStudentMemberOfTeam(newAssignee.getStudentId(), teamId)) {
                throw new BadRequestException("Assigned student is not an active member of this team");
            }
            task.setAssignedTo(newAssignee);

            // Notify if new assignee
            if (oldAssignee == null || !oldAssignee.getStudentId().equals(newAssignee.getStudentId())) {
                notifyAssignee(newAssignee, task.getTeam(), task, currentStudent);
            }
        }

        TeamTask saved = teamTaskRepository.save(task);
        TeamTaskResponse response = mapToResponse(saved);

        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/tasks", response);
        return response;
    }

    public TeamTaskResponse updateTaskStatus(String email, Long teamId, Long taskId, UpdateTaskStatusRequest req) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Task does not belong to team: " + teamId);
        }

        try {
            task.setStatus(TeamTask.TaskStatus.valueOf(req.getStatus().toUpperCase()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid task status: " + req.getStatus());
        }

        TeamTask saved = teamTaskRepository.save(task);
        TeamTaskResponse response = mapToResponse(saved);

        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/tasks", response);
        return response;
    }

    public void deleteTask(String email, Long teamId, Long taskId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("Task does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, task.getTeam(), task.getCreatedBy().getStudentId(), "delete this task");

        teamTaskRepository.delete(task);

        TeamTaskResponse deleteNotice = TeamTaskResponse.builder()
                .taskId(taskId)
                .teamId(teamId)
                .title("DELETED")
                .build();
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/tasks", deleteNotice);
    }

    private void notifyAssignee(Student assignee, Team team, TeamTask task, Student assigner) {
        try {
            Notification notif = Notification.builder()
                    .recipient(assignee)
                    .notificationType("TASK")
                    .title("New Task Assigned: " + task.getTitle())
                    .message(assigner.getName() + " assigned you a task in " + team.getTeamName())
                    .relatedEntityId(task.getTaskId())
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notif);
            com.aceteamup.dto.workspace.NotificationResponse dto = com.aceteamup.dto.workspace.NotificationResponse.builder()
                    .notificationId(saved.getNotificationId())
                    .recipientId(assignee.getStudentId())
                    .notificationType(saved.getNotificationType())
                    .title(saved.getTitle())
                    .message(saved.getMessage())
                    .relatedEntityId(saved.getRelatedEntityId())
                    .isRead(saved.getIsRead())
                    .createdAt(saved.getCreatedAt())
                    .build();
            messagingTemplate.convertAndSend("/topic/notifications/" + assignee.getStudentId(), dto);
        } catch (Exception e) {
            log.warn("Failed to notify assignee {}: {}", assignee.getStudentId(), e.getMessage());
        }
    }

    public TeamTaskResponse mapToResponse(TeamTask t) {
        return TeamTaskResponse.builder()
                .taskId(t.getTaskId())
                .teamId(t.getTeam().getTeamId())
                .title(t.getTitle())
                .description(t.getDescription())
                .priority(t.getPriority().name())
                .status(t.getStatus().name())
                .dueDate(t.getDueDate())
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getStudentId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getName() : null)
                .createdById(t.getCreatedBy().getStudentId())
                .createdByName(t.getCreatedBy().getName())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}

