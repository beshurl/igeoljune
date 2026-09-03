package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** PREF-002 추출 취향 일괄 저장 */
public record PreferenceBulkCreateRequest(
        @NotNull(message = "출처 유형은 필수입니다.")
        SourceType sourceType,

        @NotEmpty(message = "저장할 취향이 최소 1건 필요합니다.")
        @Valid
        List<Item> items
) {
    public record Item(
            @NotNull(message = "취향 유형은 필수입니다.")
            PreferenceType preferenceType,

            @NotBlank(message = "취향 값은 필수입니다.")
            @Size(max = 255)
            String preferenceValue
    ) {
    }
}
