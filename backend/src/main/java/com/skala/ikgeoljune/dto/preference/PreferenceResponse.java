package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import com.skala.ikgeoljune.domain.StructuredPreference;

import java.time.OffsetDateTime;

public record PreferenceResponse(
        Long preferenceId,
        Long recipientId,
        PreferenceType preferenceType,
        String preferenceValue,
        SourceType sourceType,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PreferenceResponse from(StructuredPreference preference) {
        return new PreferenceResponse(
                preference.getId(),
                preference.getRecipient().getId(),
                preference.getPreferenceType(),
                preference.getPreferenceValue(),
                preference.getSourceType(),
                preference.getCreatedAt(),
                preference.getUpdatedAt()
        );
    }
}
