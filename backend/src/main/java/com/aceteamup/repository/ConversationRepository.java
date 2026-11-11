package com.aceteamup.repository;

import com.aceteamup.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT cm1.conversation FROM ConversationMember cm1 " +
           "JOIN ConversationMember cm2 ON cm1.conversation.conversationId = cm2.conversation.conversationId " +
           "WHERE cm1.student.studentId = :s1 AND cm2.student.studentId = :s2")
    Optional<Conversation> findConversationBetweenStudents(@Param("s1") Long student1Id, @Param("s2") Long student2Id);
}

