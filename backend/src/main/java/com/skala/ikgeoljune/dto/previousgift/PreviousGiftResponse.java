package com.skala.ikgeoljune.dto.previousgift;

import com.skala.ikgeoljune.domain.PreviousGift;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PreviousGiftResponse(
        Long previousGiftId,
        Long recipientId,
        String giftName,
        String giftCategory,
        LocalDate giftedAt,
        String note,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PreviousGiftResponse from(PreviousGift gift) {
        return new PreviousGiftResponse(
                gift.getId(),
                gift.getRecipient().getId(),
                gift.getGiftName(),
                gift.getGiftCategory(),
                gift.getGiftedAt(),
                gift.getNote(),
                gift.getCreatedAt(),
                gift.getUpdatedAt()
        );
    }
}
