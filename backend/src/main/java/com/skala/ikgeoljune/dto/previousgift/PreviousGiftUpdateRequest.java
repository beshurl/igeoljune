package com.skala.ikgeoljune.dto.previousgift;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skala.ikgeoljune.common.validation.NotBlankIfPresent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** API.yml PreviousGiftUpdateRequest — 보낸 필드만 수정한다. */
public record PreviousGiftUpdateRequest(
        @NotBlankIfPresent(message = "선물명은 공백일 수 없습니다.")
        @Size(max = 255) String giftName,

        @Size(max = 100) String giftCategory,
        LocalDate giftedAt,
        String note
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "수정할 필드를 최소 한 개 이상 보내야 합니다.")
    public boolean isAnyFieldPresent() {
        return giftName != null || giftCategory != null || giftedAt != null || note != null;
    }
}
