package com.aceteamup.repository;

import com.aceteamup.entity.Invitation;
import com.aceteamup.entity.Invitation.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Invitation}.
 */
@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    /**
     * Inbox view: all invitations received by a student with a given status.
     * Common call: {@code findByReceiverStudentIdAndInvitationStatus(id, InvitationStatus.Pending)}
     */
    List<Invitation> findByReceiverStudentIdAndInvitationStatus(
            Long receiverId, InvitationStatus invitationStatus);

    /**
     * Check (or fetch) an invitation for a specific team-receiver pair with a given status.
     * Used to guard against duplicate Pending invitations and to look up an
     * invitation before updating its status.
     */
    Optional<Invitation> findByTeamTeamIdAndReceiverStudentIdAndInvitationStatus(
            Long teamId, Long receiverId, InvitationStatus invitationStatus);

    /** Outbox view: all invitations sent by a student. */
    List<Invitation> findBySenderStudentId(Long senderId);

    /** All invitations (any status) for a team. */
    List<Invitation> findByTeamTeamId(Long teamId);
}

