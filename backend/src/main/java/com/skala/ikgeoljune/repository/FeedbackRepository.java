package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByCandidateId(Long candidateId);

    List<Feedback> findByCandidateIdIn(List<Long> candidateIds);

    /** RECOMMEND-004: 이전 추천에서 DISLIKE 로 등록된 피드백만 조회한다. */
    @Query("""
            select f from Feedback f
            join fetch f.candidate c
            where c.recommendation.id = :recommendationId
              and f.feedbackType = com.skala.ikgeoljune.domain.FeedbackType.DISLIKE
            """)
    List<Feedback> findDislikesByRecommendationId(@Param("recommendationId") Long recommendationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Feedback f where f.candidate.id in (select c.id from RecommendationCandidate c where c.recommendation.id in :recommendationIds)")
    void deleteByRecommendationIds(@Param("recommendationIds") List<Long> recommendationIds);
}
