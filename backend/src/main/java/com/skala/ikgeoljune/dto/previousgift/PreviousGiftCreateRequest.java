package com.skala.ikgeoljune.dto.previousgift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** PREVGIFT-001 과거 선물 등록 */
public record PreviousGiftCreateRequest(
        @NotBlank(message = "선물명은 필수입니다.")
        @Size(max = 255)
        String giftName,

        @Size(max = 100)
        String giftCategory,

        LocalDate giftedAt,

        String note
) {
}
