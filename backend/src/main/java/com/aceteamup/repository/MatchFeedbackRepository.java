package com.aceteamup.repository;

import com.aceteamup.entity.MatchFeedback;
import com.aceteamup.entity.MatchFeedback.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link MatchFeedback}.
 */
@Repository
public interface MatchFeedbackRepository extends JpaRepository<MatchFeedback, Long> {

    /**
     * Retrieve all feedback a student has given for a specific event.
     * Used by the AI pipeline to avoid re-showing dismissed cards.
     */
    List<MatchFeedback> findByStudentStudentIdAndEventEventId(Long studentId, Long eventId);

    /**
     * Check whether the student has already reacted to a specific recommendation.
     * Guards against duplicate feedback submissions.
     */
    Optional<MatchFeedback> findByStudentStudentIdAndRecommendedStudentStudentIdAndEventEventId(
            Long studentId, Long recommendedStudentId, Long eventId);

    /**
     * Aggregate feedback by type for model retraining analytics.
     */
    List<MatchFeedback> findByEventEventIdAndFeedbackType(Long eventId, FeedbackType feedbackType);
}

