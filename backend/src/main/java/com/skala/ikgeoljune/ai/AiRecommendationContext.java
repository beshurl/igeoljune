package com.skala.ikgeoljune.ai;

import java.util.List;

/**
 * AI 추천 입력 콘텍스트.
 * §8 에 정의된 recipient / structured_preference / previous_gifts / gift_conditions 를 모두 담는다.
 * 재추천(RECOMMEND-004)일 때만 dislikedCandidates 가 채워진다.
 */
public record AiRecommendationContext(
        long seed,
        AiRecipientProfile recipient,
        List<AiPreference> preferences,
        List<AiPreviousGift> previousGifts,
        AiGiftConditionSpec condition,
        List<AiDislikedCandidate> dislikedCandidates,
        int candidateCount
) {
    public boolean isReRecommendation() {
        return !dislikedCandidates.isEmpty();
    }
}
