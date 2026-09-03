package com.skala.ikgeoljune.service;

/** AI 추천 실행 요청 이벤트. 트랜잭션 커밋 이후 비동기로 처리된다. */
public record RecommendationRequestedEvent(Long recommendationId) {
}
