package com.skala.ikgeoljune.dto.condition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** CONDITION-001 추천 조건 생성 */
public record GiftConditionCreateRequest(
        @NotNull(message = "최소 예산은 필수입니다.")
        @PositiveOrZero(message = "최소 예산은 0 이상이어야 합니다.")
        Integer budgetMin,

        @NotNull(message = "최대 예산은 필수입니다.")
        @PositiveOrZero(message = "최대 예산은 0 이상이어야 합니다.")
        Integer budgetMax,

        @NotBlank(message = "선물 상황은 필수입니다.")
        @Size(max = 100)
        String occasionType,

        LocalDate occasionDate,

        String preferenceNote,

        String avoidGiftNote
) {
}
