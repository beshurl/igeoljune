package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.RecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationCandidateRepository extends JpaRepository<RecommendationCandidate, Long> {

    List<RecommendationCandidate> findByRecommendationIdOrderByRecommendationRankAsc(Long recommendationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RecommendationCandidate c where c.recommendation.id in :recommendationIds")
    void deleteByRecommendationIds(@Param("recommendationIds") List<Long> recommendationIds);
}
