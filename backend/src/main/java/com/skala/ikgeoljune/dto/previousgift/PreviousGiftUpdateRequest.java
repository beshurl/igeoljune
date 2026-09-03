package com.skala.ikgeoljune.dto.previousgift;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** PREVGIFT-003 과거 선물 수정 — 보낸 필드만 수정한다. */
public record PreviousGiftUpdateRequest(
        @Size(max = 255) String giftName,
        @Size(max = 100) String giftCategory,
        LocalDate giftedAt,
        String note
) {
}
