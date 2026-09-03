package com.skala.ikgeoljune.ai;

import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;

/** AI 에 전달하는 structured_preference 취향 (§8) */
public record AiPreference(
        PreferenceType preferenceType,
        String preferenceValue,
        SourceType sourceType
) {
}
