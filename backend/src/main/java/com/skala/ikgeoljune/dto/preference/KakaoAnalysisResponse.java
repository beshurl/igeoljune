package com.skala.ikgeoljune.dto.preference;

import java.util.List;

/**
 * KAKAO-001 응답.
 *
 * <p>명세서 예시와 FE 계약(API.yml KakaoAnalysisResponse) 모두 {@code items} 만 가진다.
 * 저장 전 분석 결과라 totalCount 를 붙이지 않는다.
 */
public record KakaoAnalysisResponse(
        List<ExtractedPreferenceResponse> items
) {
    public static KakaoAnalysisResponse of(List<ExtractedPreferenceResponse> items) {
        return new KakaoAnalysisResponse(items);
    }
}
