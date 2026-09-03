package com.skala.ikgeoljune.dto.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skala.ikgeoljune.common.validation.NotBlankIfPresent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** API.yml GiftConditionUpdateRequest — 보낸 필드만 수정한다. */
public record GiftConditionUpdateRequest(
        @PositiveOrZero(message = "최소 예산은 0 이상이어야 합니다.") Integer budgetMin,
        @PositiveOrZero(message = "최대 예산은 0 이상이어야 합니다.") Integer budgetMax,

        @NotBlankIfPresent(message = "선물 상황은 공백일 수 없습니다.")
        @Size(max = 100) String occasionType,

        LocalDate occasionDate,
        String preferenceNote,
        String avoidGiftNote
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "수정할 필드를 최소 한 개 이상 보내야 합니다.")
    public boolean isAnyFieldPresent() {
        return budgetMin != null || budgetMax != null || occasionType != null
                || occasionDate != null || preferenceNote != null || avoidGiftNote != null;
    }
}
