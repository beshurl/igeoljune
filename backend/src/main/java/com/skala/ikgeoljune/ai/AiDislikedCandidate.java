package com.skala.ikgeoljune.ai;

import com.skala.ikgeoljune.domain.DislikeReason;

/** RECOMMEND-004: 이전 추천에서 DISLIKE 로 등록된 후보의 상품명과 사유 */
public record AiDislikedCandidate(
        String giftName,
        String giftCategory,
        DislikeReason dislikeReason
) {
}
