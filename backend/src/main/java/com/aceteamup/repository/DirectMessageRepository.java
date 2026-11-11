package com.aceteamup.repository;

import com.aceteamup.entity.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    List<DirectMessage> findByConversationConversationIdOrderByCreatedAtAsc(Long conversationId);
    Page<DirectMessage> findByConversationConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(dm) FROM DirectMessage dm WHERE dm.conversation.conversationId = :convId AND dm.sender.studentId <> :studentId AND dm.isRead = false")
    long countUnreadMessages(@Param("convId") Long conversationId, @Param("studentId") Long studentId);

    @Modifying
    @Query("UPDATE DirectMessage dm SET dm.isRead = true WHERE dm.conversation.conversationId = :convId AND dm.sender.studentId <> :studentId AND dm.isRead = false")
    void markAllAsRead(@Param("convId") Long conversationId, @Param("studentId") Long studentId);
}

