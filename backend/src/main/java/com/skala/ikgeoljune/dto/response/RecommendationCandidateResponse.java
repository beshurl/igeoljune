package com.skala.ikgeoljune.dto.response;

import com.skala.ikgeoljune.domain.RecommendationCandidate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationCandidateResponse {
    private Long id;
    private String name;
    private Integer price;
    private String reason;
    private Boolean liked;

    public static RecommendationCandidateResponse from(RecommendationCandidate c) {
        return RecommendationCandidateResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .price(c.getPrice())
                .reason(c.getReason())
                .liked(c.getLiked())
                .build();
    }
}
