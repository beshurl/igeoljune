package com.skala.ikgeoljune.dto.preference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skala.ikgeoljune.common.validation.NotBlankIfPresent;
import com.skala.ikgeoljune.domain.PreferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/** API.yml PreferenceUpdateRequest — 보낸 필드만 수정한다. */
public record PreferenceUpdateRequest(
        PreferenceType preferenceType,

        @NotBlankIfPresent(message = "취향 값은 공백일 수 없습니다.")
        @Size(max = 255) String preferenceValue
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "수정할 필드를 최소 한 개 이상 보내야 합니다.")
    public boolean isAnyFieldPresent() {
        return preferenceType != null || preferenceValue != null;
    }
}
