package com.skala.ikgeoljune.dto.recipient;

import com.skala.ikgeoljune.domain.Recipient;

import java.time.OffsetDateTime;

public record RecipientResponse(
        Long recipientId,
        String name,
        String relationship,
        String ageGroup,
        String gender,
        String job,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static RecipientResponse from(Recipient recipient) {
        return new RecipientResponse(
                recipient.getId(),
                recipient.getName(),
                recipient.getRelationship(),
                recipient.getAgeGroup(),
                recipient.getGender(),
                recipient.getJob(),
                recipient.getCreatedAt(),
                recipient.getUpdatedAt()
        );
    }
}
