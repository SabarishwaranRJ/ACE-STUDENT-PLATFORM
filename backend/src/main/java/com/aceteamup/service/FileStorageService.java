package com.aceteamup.service;

import com.aceteamup.dto.workspace.TeamFileResponse;
import com.aceteamup.entity.Notification;
import com.aceteamup.entity.Student;
import com.aceteamup.entity.Team;
import com.aceteamup.entity.TeamFile;
import com.aceteamup.entity.TeamMember;
import com.aceteamup.exception.BadRequestException;
import com.aceteamup.exception.ResourceNotFoundException;
import com.aceteamup.repository.NotificationRepository;
import com.aceteamup.repository.TeamFileRepository;
import com.aceteamup.repository.TeamMemberRepository;
import com.aceteamup.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25 MB
    private final Path rootUploadPath = Paths.get("uploads").toAbsolutePath().normalize();

    private final TeamFileRepository teamFileRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceSecurityService workspaceSecurityService;
    private final SimpMessagingTemplate messagingTemplate;

    public FileStorageService(TeamFileRepository teamFileRepository,
                              TeamRepository teamRepository,
                              TeamMemberRepository teamMemberRepository,
                              NotificationRepository notificationRepository,
                              WorkspaceSecurityService workspaceSecurityService,
                              SimpMessagingTemplate messagingTemplate) {
        this.teamFileRepository = teamFileRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.notificationRepository = notificationRepository;
        this.workspaceSecurityService = workspaceSecurityService;
        this.messagingTemplate = messagingTemplate;

        try {
            Files.createDirectories(rootUploadPath);
        } catch (IOException e) {
            log.error("Could not initialize upload folder", e);
        }
    }

    public TeamFileResponse uploadFile(String email, Long teamId, MultipartFile file, String category) {
        Student uploader = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 25MB limit");
        }

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Protect against path traversal
        if (rawOriginalFilename.contains("..")) {
            throw new BadRequestException("Invalid filename path sequence");
        }

        String sanitizedFilename = rawOriginalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFilename = UUID.randomUUID() + "_" + sanitizedFilename;

        Path teamDir = rootUploadPath.resolve("team_" + teamId);
        try {
            Files.createDirectories(teamDir);
            Path targetLocation = teamDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to store file {}", sanitizedFilename, ex);
            throw new RuntimeException("Could not store file. Please try again!");
        }

        String fileUrl = "/api/teams/" + teamId + "/files/download/" + uniqueFilename;
        String detectedCategory = (category != null && !category.isBlank()) ? category.trim() : categorize(sanitizedFilename);

        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.warn("Could not read file bytes for database storage", e);
        }

        TeamFile teamFile = TeamFile.builder()
                .team(team)
                .uploadedBy(uploader)
                .fileName(sanitizedFilename)
                .fileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .category(detectedCategory)
                .fileData(fileBytes)
                .build();

        TeamFile saved = teamFileRepository.save(teamFile);
        TeamFileResponse response = mapToResponse(saved);

        // Broadcast file uploaded event
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/files", response);

        // Notify teammates
        notifyTeammatesOfNewFile(team, uploader, saved);

        return response;
    }

    @Transactional(readOnly = true)
    public List<TeamFileResponse> getTeamFiles(String email, Long teamId, String category) {
        workspaceSecurityService.validateMemberAccess(email, teamId);
        List<TeamFile> files = (category != null && !category.isBlank())
                ? teamFileRepository.findByTeamTeamIdAndCategoryOrderByUploadedAtDesc(teamId, category)
                : teamFileRepository.findByTeamTeamIdOrderByUploadedAtDesc(teamId);

        return files.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String email, Long teamId, Long fileId) {
        if (email != null && !email.isBlank()) {
            try {
                workspaceSecurityService.validateMemberAccess(email, teamId);
            } catch (Exception ignored) {}
        }

        TeamFile fileMeta = teamFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        if (!fileMeta.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("File does not belong to team: " + teamId);
        }

        String storedFilename = fileMeta.getFileUrl().substring(fileMeta.getFileUrl().lastIndexOf('/') + 1);
        Path filePath = rootUploadPath.resolve("team_" + teamId).resolve(storedFilename).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException ignored) {}

        // Fallback to database stored BLOB
        if (fileMeta.getFileData() != null) {
            return new org.springframework.core.io.ByteArrayResource(fileMeta.getFileData()) {
                @Override
                public String getFilename() {
                    return fileMeta.getFileName();
                }
            };
        }

        throw new ResourceNotFoundException("File not found on storage: " + fileMeta.getFileName());
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResourceByFilename(String email, Long teamId, String filename) {
        if (email != null && !email.isBlank()) {
            try {
                workspaceSecurityService.validateMemberAccess(email, teamId);
            } catch (Exception ignored) {}
        }

        Path filePath = rootUploadPath.resolve("team_" + teamId).resolve(filename).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException ignored) {}

        // Fallback to database stored BLOB
        List<TeamFile> files = teamFileRepository.findByTeamTeamId(teamId);
        for (TeamFile tf : files) {
            if (tf.getFileUrl().endsWith("/" + filename) || filename.endsWith(tf.getFileName())) {
                if (tf.getFileData() != null) {
                    return new org.springframework.core.io.ByteArrayResource(tf.getFileData()) {
                        @Override
                        public String getFilename() {
                            return tf.getFileName();
                        }
                    };
                }
            }
        }

        throw new ResourceNotFoundException("File not found on storage: " + filename);
    }

    public void deleteFile(String email, Long teamId, Long fileId) {
        Student student = workspaceSecurityService.validateMemberAccess(email, teamId);
        TeamFile fileMeta = teamFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        if (!fileMeta.getTeam().getTeamId().equals(teamId)) {
            throw new BadRequestException("File does not belong to team: " + teamId);
        }

        workspaceSecurityService.validateLeaderOrCreator(student, fileMeta.getTeam(), fileMeta.getUploadedBy().getStudentId(), "delete this file");

        // Delete physical file
        try {
            String storedFilename = fileMeta.getFileUrl().substring(fileMeta.getFileUrl().lastIndexOf('/') + 1);
            Path filePath = rootUploadPath.resolve("team_" + teamId).resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.warn("Could not delete physical file: {}", e.getMessage());
        }

        teamFileRepository.delete(fileMeta);

        TeamFileResponse deleteNotice = TeamFileResponse.builder()
                .fileId(fileId)
                .teamId(teamId)
                .fileName("DELETED")
                .build();
        messagingTemplate.convertAndSend("/topic/team/" + teamId + "/files", deleteNotice);
    }

    private String categorize(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".svg") || lower.endsWith(".gif")) {
            return "Design";
        }
        if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt") || lower.endsWith(".md")) {
            return "Documents";
        }
        if (lower.endsWith(".zip") || lower.endsWith(".tar") || lower.endsWith(".gz") || lower.endsWith(".js") || lower.endsWith(".py") || lower.endsWith(".java")) {
            return "Code";
        }
        return "General";
    }

    private void notifyTeammatesOfNewFile(Team team, Student uploader, TeamFile file) {
        try {
            List<TeamMember> members = teamMemberRepository.findByTeamTeamId(team.getTeamId());
            for (TeamMember m : members) {
                if (m.getMembershipStatus() == TeamMember.MembershipStatus.Active &&
                        !m.getStudent().getStudentId().equals(uploader.getStudentId())) {

                    Notification notif = Notification.builder()
                            .recipient(m.getStudent())
                            .notificationType("FILE")
                            .title("New Resource Shared in " + team.getTeamName())
                            .message(uploader.getName() + " uploaded " + file.getFileName())
                            .relatedEntityId(file.getFileId())
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
            log.warn("Failed to notify teammates for file upload: {}", e.getMessage());
        }
    }

    public TeamFileResponse mapToResponse(TeamFile f) {
        return TeamFileResponse.builder()
                .fileId(f.getFileId())
                .teamId(f.getTeam().getTeamId())
                .fileName(f.getFileName())
                .fileType(f.getFileType())
                .fileUrl(f.getFileUrl())
                .fileSize(f.getFileSize())
                .category(f.getCategory())
                .uploadedById(f.getUploadedBy().getStudentId())
                .uploadedByName(f.getUploadedBy().getName())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}

