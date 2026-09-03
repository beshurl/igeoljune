package com.skala.ikgeoljune.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 추천 레코드가 커밋된 뒤에 AI 처리를 시작한다.
 * 덕분에 컨트롤러는 202 Accepted 를 바로 반환할 수 있다.
 */
@Component
@RequiredArgsConstructor
public class RecommendationEventListener {

    private final RecommendationProcessor recommendationProcessor;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRecommendationRequested(RecommendationRequestedEvent event) {
        recommendationProcessor.process(event.recommendationId());
    }
}
