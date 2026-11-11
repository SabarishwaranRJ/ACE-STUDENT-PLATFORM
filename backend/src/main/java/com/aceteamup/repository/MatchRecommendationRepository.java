package com.aceteamup.repository;

import com.aceteamup.entity.MatchRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link MatchRecommendation}.
 */
@Repository
public interface MatchRecommendationRepository extends JpaRepository<MatchRecommendation, Long> {

    /**
     * Core recommendation feed query: returns all recommendations for a student
     * in a given event, ranked from highest to lowest compatibility score.
     */
    List<MatchRecommendation> findByStudentStudentIdAndEventEventIdOrderByCompatibilityScoreDesc(
            Long studentId, Long eventId);

    /**
     * Find all events for which a student has at least one recommendation.
     * Useful for the student's personalised dashboard.
     */
    List<MatchRecommendation> findByStudentStudentId(Long studentId);

    /**
     * Check whether a recommendation already exists for a student triplet
     * (to avoid duplicate inserts from the AI pipeline).
     */
    boolean existsByStudentStudentIdAndRecommendedStudentStudentIdAndEventEventId(
            Long studentId, Long recommendedStudentId, Long eventId);
}

