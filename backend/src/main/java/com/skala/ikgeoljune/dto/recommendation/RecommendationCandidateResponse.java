package com.skala.ikgeoljune.dto.recommendation;

import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.feedback.CandidateFeedbackResponse;

/** RECOMMEND-002 candidates 항목 */
public record RecommendationCandidateResponse(
        Long candidateId,
        String giftName,
        String giftCategory,
        Integer estimatedPriceMin,
        Integer estimatedPriceMax,
        String recommendationReason,
        String consideredInfo,
        String cautionNote,
        Integer recommendationRank,
        CandidateFeedbackResponse feedback
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
                candidate.getRecommendationRank(),
                feedback == null ? null : CandidateFeedbackResponse.from(feedback)
        );
    }
}
