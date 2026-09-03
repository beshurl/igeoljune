package com.skala.ikgeoljune.ai;

import java.time.LocalDate;

/** AI 에 전달하는 gift_conditions 현재 추천 조건 (§8) */
public record AiGiftConditionSpec(
        Integer budgetMin,
        Integer budgetMax,
        String occasionType,
        LocalDate occasionDate,
        String preferenceNote,
        String avoidGiftNote
) {
}
