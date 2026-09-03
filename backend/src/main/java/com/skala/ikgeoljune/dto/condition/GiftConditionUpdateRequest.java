package com.skala.ikgeoljune.dto.condition;

import com.skala.ikgeoljune.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** CONDITION-003 추천 조건 수정 — 보낸 필드만 수정한다. */
public record GiftConditionUpdateRequest(
        @PositiveOrZero(message = "최소 예산은 0 이상이어야 합니다.") Integer budgetMin,
        @PositiveOrZero(message = "최대 예산은 0 이상이어야 합니다.") Integer budgetMax,

        @NotBlankIfPresent(message = "선물 상황은 공백일 수 없습니다.")
        @Size(max = 100) String occasionType,

        LocalDate occasionDate,
        String preferenceNote,
        String avoidGiftNote
) {
}
