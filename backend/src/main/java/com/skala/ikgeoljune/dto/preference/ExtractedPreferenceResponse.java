package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.PreferenceType;

/**
 * KAKAO-001 분석 결과 항목.
 * 저장 전 상태이므로 preferenceId 가 없다. 사용자가 확인한 항목만 PREF-002 로 저장한다.
 */
public record ExtractedPreferenceResponse(
        PreferenceType preferenceType,
        String preferenceValue
) {
}
