package com.aceteamup.dto.workspace;

import com.aceteamup.dto.team.TeamMemberResponse;
import com.aceteamup.dto.team.TeamResponse;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceOverviewResponse {
    private TeamResponse team;
    private boolean isCurrentUserLeader;
    private String currentUserRole;
    private List<TeamMemberResponse> members;
    private TaskSummaryDto taskSummary;
    private List<TeamAnnouncementResponse> recentAnnouncements;
    private List<TeamMeetingResponse> upcomingMeetings;
    private List<TeamFileResponse> recentFiles;
    private long totalMessages;
}

