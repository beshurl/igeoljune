package com.skala.ikgeoljune.dto.recommendation;

import com.skala.ikgeoljune.domain.Recommendation;
import com.skala.ikgeoljune.domain.RecommendationStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * RECOMMEND-002 추천 결과 조회.
 * status 가 PROCESSING 또는 FAILED 이면 candidates 는 빈 배열이다.
 */
public record RecommendationDetailResponse(
        Long recommendationId,
        Long conditionId,
        Long previousRecommendationId,
        RecommendationStatus status,
        OffsetDateTime createdAt,
        List<RecommendationCandidateResponse> candidates
) {
    public static RecommendationDetailResponse of(Recommendation recommendation,
                                                  List<RecommendationCandidateResponse> candidates) {
        return new RecommendationDetailResponse(
                recommendation.getId(),
                recommendation.getCondition().getId(),
                recommendation.getPreviousRecommendationId(),
                recommendation.getStatus(),
                recommendation.getCreatedAt(),
                candidates
        );
    }
}
