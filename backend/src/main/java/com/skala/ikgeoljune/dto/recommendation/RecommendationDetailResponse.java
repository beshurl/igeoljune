package com.skala.ikgeoljune.dto.recommendation;

import com.skala.ikgeoljune.domain.Recommendation;
import com.skala.ikgeoljune.domain.RecommendationStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * RECOMMEND-002 추천 결과 조회.
 *
 * <p>status 가 PROCESSING 또는 FAILED 이면 candidates 는 빈 배열이다.
 * FAILED 일 때만 {@code failure} 에 실패 사유가 채워지고, 그 외에는 null 이다.
 */
public record RecommendationDetailResponse(
        Long recommendationId,
        Long conditionId,
        Long previousRecommendationId,
        RecommendationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Failure failure,
        List<RecommendationCandidateResponse> candidates
) {
    /** §1.4 오류 응답과 동일한 code / message 체계를 따른다. */
    public record Failure(String code, String message) {
    }

    public static RecommendationDetailResponse of(Recommendation recommendation,
                                                  List<RecommendationCandidateResponse> candidates) {
        Failure failure = null;
        if (recommendation.getStatus() == RecommendationStatus.FAILED) {
            failure = new Failure(recommendation.getFailureCode(), recommendation.getFailureMessage());
        }
        return new RecommendationDetailResponse(
                recommendation.getId(),
                recommendation.getCondition().getId(),
                recommendation.getPreviousRecommendationId(),
                recommendation.getStatus(),
                recommendation.getCreatedAt(),
                recommendation.getUpdatedAt(),
                failure,
                candidates
        );
    }
}
