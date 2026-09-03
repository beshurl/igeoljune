package com.skala.ikgeoljune.ai;

import java.util.List;

/**
 * AI 연동 경계.
 * 현재는 {@link com.skala.ikgeoljune.ai.mock.MockGiftAiClient} 가 구현하며,
 * 실제 LLM 연동 시 이 인터페이스만 새로 구현하고 app.ai.provider 로 교체한다.
 */
public interface GiftAiClient {

    /** KAKAO-001 카카오톡 대화에서 취향 후보를 추출한다. */
    List<AiExtractedPreference> extractPreferences(AiKakaoAnalysisContext context);

    /** RECOMMEND-001 / RECOMMEND-004 선물 후보를 생성한다. 반환 순서가 추천 순위다. */
    List<AiGiftCandidate> recommendGifts(AiRecommendationContext context);
}
