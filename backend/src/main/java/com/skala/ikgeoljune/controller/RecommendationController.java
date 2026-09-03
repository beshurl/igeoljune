package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationDetailResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationResponse;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** §8 AI 추천·재추천 API */
@Tag(name = "Recommendation", description = "AI 추천")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "RECOMMEND-001 AI 추천 요청 (202 Accepted, status=PROCESSING)")
    @PostMapping("/gift-conditions/{conditionId}/recommendations")
    public ResponseEntity<RecommendationResponse> request(@CurrentUser AuthUser authUser,
                                                          @PathVariable Long conditionId) {
        return accepted(recommendationService.request(conditionId, authUser.userId()));
    }

    @Operation(summary = "RECOMMEND-002 추천 결과 조회")
    @GetMapping("/recommendations/{recommendationId}")
    public RecommendationDetailResponse findOne(@CurrentUser AuthUser authUser,
                                                @PathVariable Long recommendationId) {
        return recommendationService.findOne(recommendationId, authUser.userId());
    }

    @Operation(summary = "RECOMMEND-003 조건별 추천 목록 (생성일 역순)")
    @GetMapping("/gift-conditions/{conditionId}/recommendations")
    public ListResponse<RecommendationResponse> findAllByCondition(@CurrentUser AuthUser authUser,
                                                                   @PathVariable Long conditionId) {
        return recommendationService.findAllByCondition(conditionId, authUser.userId());
    }

    @Operation(summary = "RECOMMEND-004 피드백 반영 재추천 (202 Accepted)")
    @PostMapping("/recommendations/{recommendationId}/re-recommend")
    public ResponseEntity<RecommendationResponse> reRecommend(@CurrentUser AuthUser authUser,
                                                              @PathVariable Long recommendationId) {
        return accepted(recommendationService.reRecommend(recommendationId, authUser.userId()));
    }

    /** API.yml: 202 응답에 상태 조회 URI 를 Location 헤더로 함께 내려준다. */
    private ResponseEntity<RecommendationResponse> accepted(RecommendationResponse response) {
        return ResponseEntity.accepted()
                .header(HttpHeaders.LOCATION, "/api/v1/recommendations/" + response.recommendationId())
                .body(response);
    }
}
