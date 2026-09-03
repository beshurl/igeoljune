package com.skala.ikgeoljune.dto.recommendation;

import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.feedback.FeedbackResponse;

import java.time.OffsetDateTime;

/** API.yml GiftCandidate */
public record RecommendationCandidateResponse(
        Long candidateId,
        String giftName,
        String giftCategory,
        Integer estimatedPriceMin,
        Integer estimatedPriceMax,
        String recommendationReason,
        String consideredInfo,
        String cautionNote,
        Integer recommendRank,
        OffsetDateTime createdAt,
        FeedbackResponse feedback
) {
    public static RecommendationCandidateResponse of(RecommendationCandidate candidate, Feedback feedback) {
        return new RecommendationCandidateResponse(
                candidate.getId(),
                candidate.getGiftName(),
                candidate.getGiftCategory(),
                candidate.getEstimatedPriceMin(),
                candidate.getEstimatedPriceMax(),
                candidate.getRecommendationReason(),
                candidate.getConsideredInfo(),
                candidate.getCautionNote(),
                candidate.getRecommendRank(),
                candidate.getCreatedAt(),
                feedback == null ? null : FeedbackResponse.from(feedback)
        );
    }
}
