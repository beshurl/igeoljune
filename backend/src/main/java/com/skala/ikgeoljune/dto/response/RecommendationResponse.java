package com.skala.ikgeoljune.dto.response;

import com.skala.ikgeoljune.domain.Recommendation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// SCR-AI-001 ★핵심 응답 — status(pending/completed/failed) 필드로 AI 비동기 파이프라인 표현
@Getter
@Builder
public class RecommendationResponse {
    private Long id;
    private String status;
    private List<RecommendationCandidateResponse> candidates;

    public static RecommendationResponse from(Recommendation r) {
        return RecommendationResponse.builder()
                .id(r.getId())
                .status(r.getStatus().name().toLowerCase())
                .candidates(r.getCandidates().stream()
                        .map(RecommendationCandidateResponse::from)
                        .toList())
                .build();
    }
}
