package com.aceteamup.service;

import com.aceteamup.dto.team.TeamMemberResponse;
import com.aceteamup.dto.workspace.*;
import com.aceteamup.entity.*;
import com.aceteamup.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceSecurityService workspaceSecurityService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamService teamService;
    private final TeamTaskRepository teamTaskRepository;
    private final TeamAnnouncementRepository teamAnnouncementRepository;
    private final TeamMeetingRepository teamMeetingRepository;
    private final TeamFileRepository teamFileRepository;
    private final TeamMessageRepository teamMessageRepository;

    public WorkspaceService(WorkspaceSecurityService workspaceSecurityService,
                            TeamRepository teamRepository,
                            TeamMemberRepository teamMemberRepository,
                            TeamService teamService,
                            TeamTaskRepository teamTaskRepository,
                            TeamAnnouncementRepository teamAnnouncementRepository,
                            TeamMeetingRepository teamMeetingRepository,
                            TeamFileRepository teamFileRepository,
                            TeamMessageRepository teamMessageRepository) {
        this.workspaceSecurityService = workspaceSecurityService;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamService = teamService;
        this.teamTaskRepository = teamTaskRepository;
        this.teamAnnouncementRepository = teamAnnouncementRepository;
        this.teamMeetingRepository = teamMeetingRepository;
        this.teamFileRepository = teamFileRepository;
        this.teamMessageRepository = teamMessageRepository;
    }

    @Transactional(readOnly = true)
    public WorkspaceOverviewResponse getWorkspaceOverview(String email, Long teamId) {
        Student currentStudent = workspaceSecurityService.validateMemberAccess(email, teamId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        boolean isLeader = workspaceSecurityService.isTeamLeader(currentStudent, team);

        // Fetch team members
        List<TeamMember> members = teamMemberRepository.findByTeamTeamId(teamId);
        List<TeamMemberResponse> memberResponses = members.stream()
                .filter(m -> m.getMembershipStatus() == TeamMember.MembershipStatus.Active)
                .map(m -> TeamMemberResponse.builder()
                        .studentId(m.getStudent().getStudentId())
                        .name(m.getStudent().getName())
                        .email(m.getStudent().getEmail())
                        .assignedRole(m.getAssignedRole())
                        .membershipStatus(m.getMembershipStatus().name())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        // Find current user's role
        String currentUserRole = isLeader ? "Team Leader" : "Member";
        for (TeamMember m : members) {
            if (m.getStudent().getStudentId().equals(currentStudent.getStudentId()) && m.getAssignedRole() != null) {
                currentUserRole = m.getAssignedRole();
                break;
            }
        }

        // Task statistics
        long totalTasks = teamTaskRepository.countByTeamTeamId(teamId);
        long todoCount = teamTaskRepository.countByTeamTeamIdAndStatus(teamId, TeamTask.TaskStatus.TODO);
        long inProgressCount = teamTaskRepository.countByTeamTeamIdAndStatus(teamId, TeamTask.TaskStatus.IN_PROGRESS);
        long inReviewCount = teamTaskRepository.countByTeamTeamIdAndStatus(teamId, TeamTask.TaskStatus.IN_REVIEW);
        long completedCount = teamTaskRepository.countByTeamTeamIdAndStatus(teamId, TeamTask.TaskStatus.COMPLETED);
        int percentage = totalTasks > 0 ? (int) Math.round((completedCount * 100.0) / totalTasks) : 0;

        TaskSummaryDto taskSummary = TaskSummaryDto.builder()
                .totalTasks(totalTasks)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .inReviewCount(inReviewCount)
                .completedCount(completedCount)
                .completionPercentage(percentage)
                .build();

        // Recent Announcements
        List<TeamAnnouncementResponse> recentAnnouncements = teamAnnouncementRepository
                .findByTeamTeamIdOrderByIsPinnedDescCreatedAtDesc(teamId).stream()
                .limit(5)
                .map(a -> TeamAnnouncementResponse.builder()
                        .announcementId(a.getAnnouncementId())
                        .teamId(a.getTeam().getTeamId())
                        .title(a.getTitle())
                        .content(a.getContent())
                        .isPinned(a.getIsPinned())
                        .createdById(a.getCreatedBy().getStudentId())
                        .createdByName(a.getCreatedBy().getName())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        // Upcoming Meetings
        List<TeamMeetingResponse> upcomingMeetings = teamMeetingRepository
                .findByTeamTeamIdAndStartTimeAfterOrderByStartTimeAsc(teamId, LocalDateTime.now()).stream()
                .limit(5)
                .map(m -> TeamMeetingResponse.builder()
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
                        .build())
                .collect(Collectors.toList());

        // Recent Files
        List<TeamFileResponse> recentFiles = teamFileRepository
                .findByTeamTeamIdOrderByUploadedAtDesc(teamId).stream()
                .limit(5)
                .map(f -> TeamFileResponse.builder()
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
                        .build())
                .collect(Collectors.toList());

        long totalMessages = teamMessageRepository.countByTeamTeamId(teamId);

        return WorkspaceOverviewResponse.builder()
                .team(teamService.mapToResponse(team, memberResponses.size()))
                .isCurrentUserLeader(isLeader)
                .currentUserRole(currentUserRole)
                .members(memberResponses)
                .taskSummary(taskSummary)
                .recentAnnouncements(recentAnnouncements)
                .upcomingMeetings(upcomingMeetings)
                .recentFiles(recentFiles)
                .totalMessages(totalMessages)
                .build();
    }
}

