package com.skala.ikgeoljune.dto.preference;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * API.yml PreferenceBulkCreateRequest.
 *
 * <p>카카오 분석 검토 화면에서 승인한 항목만 저장하며, sourceType 은 서버가 KAKAO 로 설정한다.
 */
public record PreferenceBulkCreateRequest(
        @NotEmpty(message = "저장할 취향이 최소 1건 필요합니다.")
        @Size(max = 100, message = "한 번에 최대 100건까지 저장할 수 있습니다.")
        @Valid
        List<PreferenceCreateRequest> items
) {
}
