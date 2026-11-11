package com.aceteamup.repository;

import com.aceteamup.entity.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {
    List<TeamTask> findByTeamTeamIdOrderByCreatedAtDesc(Long teamId);
    List<TeamTask> findByTeamTeamIdAndStatus(Long teamId, TeamTask.TaskStatus status);
    List<TeamTask> findByAssignedToStudentId(Long studentId);
    long countByTeamTeamIdAndStatus(Long teamId, TeamTask.TaskStatus status);
    long countByTeamTeamId(Long teamId);
}

