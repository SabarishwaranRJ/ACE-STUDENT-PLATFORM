package com.aceteamup.repository;

import com.aceteamup.entity.TeamMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMessageRepository extends JpaRepository<TeamMessage, Long> {
    List<TeamMessage> findByTeamTeamIdOrderByCreatedAtAsc(Long teamId);
    Page<TeamMessage> findByTeamTeamIdOrderByCreatedAtDesc(Long teamId, Pageable pageable);
    long countByTeamTeamId(Long teamId);
}

