package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.ai.AiGiftCandidate;
import com.skala.ikgeoljune.ai.AiRecommendationContext;
import com.skala.ikgeoljune.ai.GiftAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RECOMMEND-001 / RECOMMEND-004 의 실제 AI 처리.
 * 성공하면 status 가 SUCCESS, 실패하면 FAILED 로 바뀐다. (§10 recommendationStatus)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationProcessor {

    private final RecommendationStore recommendationStore;
    private final GiftAiClient giftAiClient;

    public void process(Long recommendationId) {
        try {
            AiRecommendationContext context = recommendationStore.loadContext(recommendationId);
            List<AiGiftCandidate> results = giftAiClient.recommendGifts(context);
            recommendationStore.saveSuccess(recommendationId, results);
            log.info("AI 추천 완료 - recommendationId={}, 후보 {}건", recommendationId, results.size());
        } catch (Exception e) {
            log.error("AI 추천 실패 - recommendationId={}", recommendationId, e);
            safelyMarkFailed(recommendationId);
        }
    }

    private void safelyMarkFailed(Long recommendationId) {
        try {
            recommendationStore.markFailed(recommendationId);
        } catch (Exception e) {
            log.error("추천 실패 상태 저장 실패 - recommendationId={}", recommendationId, e);
        }
    }
}
