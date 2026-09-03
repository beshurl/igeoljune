package com.skala.ikgeoljune.ai;

import com.skala.ikgeoljune.domain.PreferenceType;

/** KAKAO-001 분석으로 추출한 취향 후보 */
public record AiExtractedPreference(
        PreferenceType preferenceType,
        String preferenceValue
) {
}
