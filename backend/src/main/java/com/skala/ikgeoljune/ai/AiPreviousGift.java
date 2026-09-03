package com.skala.ikgeoljune.ai;

import java.time.LocalDate;

/** AI 에 전달하는 previous_gifts 과거 선물 (§8) */
public record AiPreviousGift(
        String giftName,
        String giftCategory,
        LocalDate giftedAt
) {
}
