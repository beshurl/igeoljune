package com.skala.ikgeoljune.dto.preference;

import com.skala.ikgeoljune.domain.SourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * PREF-002 추출 취향 일괄 저장.
 *
 * <p>프론트와 Notion 명세서가 {@code sourceType} 을 함께 보내므로 필드를 받는다.
 * 다만 이 API 는 카카오 분석 검토 결과 전용이라 KAKAO 만 허용하고,
 * 생략하면 서버가 KAKAO 로 설정한다.
 */
public record PreferenceBulkCreateRequest(
        SourceType sourceType,

        @NotEmpty(message = "저장할 취향이 최소 1건 필요합니다.")
        @Size(max = 100, message = "한 번에 최대 100건까지 저장할 수 있습니다.")
        @Valid
        List<PreferenceCreateRequest> items
) {
    /** 생략 가능하지만, 보냈다면 KAKAO 여야 한다. */
    public SourceType resolveSourceType() {
        return sourceType == null ? SourceType.KAKAO : sourceType;
    }
}
