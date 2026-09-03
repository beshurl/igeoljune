package com.skala.ikgeoljune.ai;

/** AI 가 생성한 선물 후보 1건. 순위(recommendationRank)는 반환 순서로 결정한다. */
public record AiGiftCandidate(
        String giftName,
        String giftCategory,
        Integer estimatedPriceMin,
        Integer estimatedPriceMax,
        String recommendationReason,
        String consideredInfo,
        String cautionNote
) {
}
