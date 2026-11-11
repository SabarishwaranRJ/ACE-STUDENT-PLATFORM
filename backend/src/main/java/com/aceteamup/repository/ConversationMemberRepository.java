package com.aceteamup.repository;

import com.aceteamup.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, ConversationMember.ConversationMemberId> {
    List<ConversationMember> findByStudentStudentId(Long studentId);
    List<ConversationMember> findByConversationConversationId(Long conversationId);
    boolean existsByConversationConversationIdAndStudentStudentId(Long conversationId, Long studentId);
    Optional<ConversationMember> findByConversationConversationIdAndStudentStudentIdNot(Long conversationId, Long studentId);
}

