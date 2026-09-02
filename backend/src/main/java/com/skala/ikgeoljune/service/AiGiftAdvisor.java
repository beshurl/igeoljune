package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.GiftCondition;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 확장 지점 (UC8) — 예산을 최우선으로 조건을 종합해 선물 후보와 추천 이유를 생성한다.
 * 설계문서 §2 AI-Ready 작성 시 이 클래스의 시스템/유저 프롬프트를 그대로 인용할 것.
 */
@Component
public class AiGiftAdvisor {

    private static final String SYSTEM_PROMPT = """
            당신은 선물 추천 전문가입니다.
            주어진 예산을 반드시 최우선으로 지키고, 취향/제외 조건을 반영하여
            서로 다른 성격의 선물 후보 2~3개를 한국어로 제안하세요.
            각 후보는 이름, 예상 가격(원 단위 정수), 추천 이유(1~2문장)를 포함해야 하며
            반드시 지정된 JSON 스키마로만 응답하세요.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            예산: %d원
            취향 태그: %s
            제외 태그: %s
            기념일: %s
            """;

    private final ChatClient chatClient;

    public AiGiftAdvisor(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<GiftCandidateAi> recommend(GiftCondition condition) {
        String userPrompt = USER_PROMPT_TEMPLATE.formatted(
                condition.getBudget(),
                condition.getPreferenceTags(),
                condition.getExcludeTags(),
                condition.getAnniversaryDate()
        );

        GiftRecommendationAiResponse response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .entity(GiftRecommendationAiResponse.class);

        return response != null ? response.candidates() : List.of();
    }

    // 입출력 JSON 스키마 (설계문서 §2에 그대로 기재)
    // { "candidates": [ { "name": string, "price": number, "reason": string } ] }
    public record GiftRecommendationAiResponse(List<GiftCandidateAi> candidates) {
    }

    public record GiftCandidateAi(String name, Integer price, String reason) {
    }
}
