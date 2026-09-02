package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.request.FeedbackRequest;
import com.skala.ikgeoljune.dto.request.GiftConfirmRequest;
import com.skala.ikgeoljune.dto.response.RecommendationResponse;
import com.skala.ikgeoljune.service.GiftHistoryService;
import com.skala.ikgeoljune.service.GiftRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// SCR-AI-001 ★핵심 / SCR-AI-002 · UC8~UC13 (대표 흐름)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecommendationController {

    private final GiftRecommendationService giftRecommendationService;
    private final GiftHistoryService giftHistoryService;

    // UC8 AI 추천 요청 (AI 확장 지점)
    @PostMapping("/gift-conditions/{giftConditionId}/recommendations")
    public RecommendationResponse requestRecommendation(@PathVariable Long giftConditionId) {
        return giftRecommendationService.requestRecommendation(giftConditionId);
    }

    // UC9 추천 결과 확인
    @GetMapping("/recommendations/{recommendationId}")
    public RecommendationResponse get(@PathVariable Long recommendationId) {
        return giftRecommendationService.getRecommendation(recommendationId);
    }

    // UC10·UC11 피드백
    @PostMapping("/recommendations/{recommendationId}/feedback")
    public void feedback(@PathVariable Long recommendationId, @RequestBody FeedbackRequest request) {
        giftRecommendationService.submitFeedback(recommendationId, request);
    }

    // UC12 재추천
    @PostMapping("/recommendations/{recommendationId}/re-recommend")
    public RecommendationResponse reRecommend(@PathVariable Long recommendationId) {
        return giftRecommendationService.reRecommend(recommendationId);
    }

    // UC13 선물 확정 및 이력 저장
    @PostMapping("/recommendations/{recommendationId}/confirm")
    public Map<String, Long> confirm(@PathVariable Long recommendationId,
                                      @Valid @RequestBody GiftConfirmRequest request) {
        Long historyId = giftHistoryService.confirm(recommendationId, request);
        return Map.of("historyId", historyId);
    }
}
