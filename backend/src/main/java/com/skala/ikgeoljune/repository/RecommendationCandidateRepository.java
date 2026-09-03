package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.RecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationCandidateRepository extends JpaRepository<RecommendationCandidate, Long> {

    List<RecommendationCandidate> findByRecommendationIdOrderByRecommendRankAsc(Long recommendationId);

    /** 한 추천 실행 안에서 이미 선택된 후보 (최대 1건) */
    List<RecommendationCandidate> findByRecommendationIdAndSelectedAtIsNotNull(Long recommendationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RecommendationCandidate c where c.recommendation.id in :recommendationIds")
    void deleteByRecommendationIds(@Param("recommendationIds") List<Long> recommendationIds);
}
