package com.skala.ikgeoljune.dto.recommendation;

import com.skala.ikgeoljune.domain.Recommendation;
import com.skala.ikgeoljune.domain.RecommendationStatus;

import java.time.OffsetDateTime;

/**
 * RECOMMEND-001 / RECOMMEND-004 (202 Accepted) 및 RECOMMEND-003 목록 항목.
 * 후보 목록 없이 추천 실행 정보만 담는다.
 */
public record RecommendationResponse(
        Long recommendationId,
        Long conditionId,
        Long previousRecommendationId,
        RecommendationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static RecommendationResponse from(Recommendation recommendation) {
        return new RecommendationResponse(
                recommendation.getId(),
                recommendation.getCondition().getId(),
                recommendation.getPreviousRecommendationId(),
                recommendation.getStatus(),
                recommendation.getCreatedAt(),
                recommendation.getUpdatedAt()
        );
    }
}
