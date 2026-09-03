package com.skala.ikgeoljune.repository;

import com.skala.ikgeoljune.domain.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /** RECOMMEND-003 생성일 역순 */
    List<Recommendation> findByConditionIdOrderByCreatedAtDescIdDesc(Long conditionId);

    boolean existsByConditionId(Long conditionId);

    @Query("select r.id from Recommendation r where r.condition.id in :conditionIds")
    List<Long> findIdsByConditionIds(@Param("conditionIds") List<Long> conditionIds);

    /** 자기참조(previous_recommendation_id) 때문에 삭제 전에 링크를 먼저 끊는다. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Recommendation r set r.previousRecommendation = null where r.id in :recommendationIds")
    void clearPreviousLinks(@Param("recommendationIds") List<Long> recommendationIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Recommendation r where r.id in :recommendationIds")
    void deleteByIds(@Param("recommendationIds") List<Long> recommendationIds);
}
