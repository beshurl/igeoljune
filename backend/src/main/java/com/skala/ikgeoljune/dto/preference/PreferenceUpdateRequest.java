package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.PreferenceType;
import jakarta.validation.constraints.Size;

/** PREF-004 취향 수정 — 보낸 필드만 수정한다. */
public record PreferenceUpdateRequest(
        PreferenceType preferenceType,
        @Size(max = 255) String preferenceValue
) {
}
