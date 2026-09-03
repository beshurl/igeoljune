package com.skala.ikgeoljune.ai;

/** KAKAO-001 분석 입력. 원문(chatText)은 분석 중에만 사용하고 DB 에 저장하지 않는다. */
public record AiKakaoAnalysisContext(
        AiRecipientProfile recipient,
        String chatText
) {
}
