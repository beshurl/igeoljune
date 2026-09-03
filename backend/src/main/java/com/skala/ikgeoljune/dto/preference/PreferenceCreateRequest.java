package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.PreferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** PREF-001 취향 등록 — sourceType 은 서버가 DIRECT 로 설정한다. */
public record PreferenceCreateRequest(
        @NotNull(message = "취향 유형은 필수입니다.")
        PreferenceType preferenceType,

        @NotBlank(message = "취향 값은 필수입니다.")
        @Size(max = 255)
        String preferenceValue
) {
}
