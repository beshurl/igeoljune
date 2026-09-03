package com.skala.ikgeoljune.api;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.ai.mock.MockGiftAiClient;
import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Mock AI 가 만들어 내는 예상 가격이 카탈로그 가격대와 예산을 모두 벗어나지 않는지 검증한다. */
class MockGiftAiClientTest {

    private final MockGiftAiClient client = new MockGiftAiClient(0);

    private AiRecommendationContext contextWithBudget(int budgetMin, int budgetMax) {
        return new AiRecommendationContext(
                1L,
                new AiRecipientProfile("철수", "FRIEND", "LATE_20S", "MALE", "개발자"),
                List.of(new AiPreference(PreferenceType.INTEREST, "홈카페", SourceType.DIRECT)),
                List.of(),
                new AiGiftConditionSpec(budgetMin, budgetMax, "BIRTHDAY", null, null, null),
                List.of(),
                5
        );
    }

    @Test
    @DisplayName("예산이 카탈로그 가격대를 완전히 벗어나면 가격을 지어내지 않고 실패한다")
    void doesNotFabricatePricesOutsideCatalog() {
        assertThatThrownBy(() -> client.recommendGifts(contextWithBudget(1_000_000, 2_000_000)))
                .isInstanceOf(AiException.class)
                .hasMessageContaining("찾지 못했습니다");
    }

    @Test
    @DisplayName("예상 가격은 항상 요청 예산 범위 안에 있고 min <= max 이다")
    void estimatedPriceStaysWithinBudget() {
        List<AiGiftCandidate> candidates = client.recommendGifts(contextWithBudget(30_000, 70_000));

        assertThat(candidates).isNotEmpty();
        for (AiGiftCandidate candidate : candidates) {
            assertThat(candidate.estimatedPriceMin()).isGreaterThanOrEqualTo(30_000);
            assertThat(candidate.estimatedPriceMax()).isLessThanOrEqualTo(70_000);
            assertThat(candidate.estimatedPriceMin()).isLessThanOrEqualTo(candidate.estimatedPriceMax());
        }
    }

    @Test
    @DisplayName("예산이 아주 좁아도 겹치는 상품만 후보가 된다")
    void narrowBudgetOnlyReturnsOverlappingItems() {
        List<AiGiftCandidate> candidates = client.recommendGifts(contextWithBudget(20_000, 25_000));

        for (AiGiftCandidate candidate : candidates) {
            assertThat(candidate.estimatedPriceMin()).isBetween(20_000, 25_000);
            assertThat(candidate.estimatedPriceMax()).isBetween(20_000, 25_000);
        }
    }
}
