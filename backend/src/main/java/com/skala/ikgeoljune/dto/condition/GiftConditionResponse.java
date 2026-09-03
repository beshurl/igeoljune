package com.skala.ikgeoljune.dto.condition;

import com.skala.ikgeoljune.domain.GiftCondition;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record GiftConditionResponse(
        Long conditionId,
        Long recipientId,
        Integer budgetMin,
        Integer budgetMax,
        String occasionType,
        LocalDate occasionDate,
        String preferenceNote,
        String avoidGiftNote,
        OffsetDateTime createdAt
) {
    public static GiftConditionResponse from(GiftCondition condition) {
        return new GiftConditionResponse(
                condition.getId(),
                condition.getRecipient().getId(),
                condition.getBudgetMin(),
                condition.getBudgetMax(),
                condition.getOccasionType(),
                condition.getOccasionDate(),
                condition.getPreferenceNote(),
                condition.getAvoidGiftNote(),
                condition.getCreatedAt()
        );
    }
}
