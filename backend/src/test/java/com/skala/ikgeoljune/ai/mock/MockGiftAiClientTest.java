package com.skala.ikgeoljune.ai.mock;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Mock AI 가 카탈로그 실제 가격을 유지하는지, 예산 밖 대안을 제공하는지 검증한다. */
class MockGiftAiClientTest {

    private final MockGiftAiClient client = new MockGiftAiClient();

    private static final Map<String, GiftCatalog.Item> CATALOG_BY_NAME = GiftCatalog.ITEMS.stream()
            .collect(Collectors.toMap(GiftCatalog.Item::name, Function.identity()));

    private AiRecommendationContext context(int budgetMin, int budgetMax, List<AiPreviousGift> previousGifts) {
        return new AiRecommendationContext(
                1L,
                new AiRecipientProfile("철수", "FRIEND", "LATE_20S", "MALE", "개발자"),
                List.of(new AiPreference(PreferenceType.INTEREST, "홈카페", SourceType.DIRECT)),
                previousGifts,
                new AiGiftConditionSpec(budgetMin, budgetMax, "BIRTHDAY", null, null, null),
                List.of(),
                5
        );
    }

    /** 예상 가격은 언제나 카탈로그의 실제 가격이어야 한다. 예산으로 보정하지 않는다. */
    private void assertPricesAreCatalogPrices(List<AiGiftCandidate> candidates) {
        for (AiGiftCandidate candidate : candidates) {
            GiftCatalog.Item item = CATALOG_BY_NAME.get(candidate.giftName());
            assertThat(item).as("카탈로그에 없는 상품: %s", candidate.giftName()).isNotNull();
            assertThat(candidate.estimatedPriceMin()).isEqualTo(item.priceMin());
            assertThat(candidate.estimatedPriceMax()).isEqualTo(item.priceMax());
        }
    }

    @Test
    @DisplayName("예산이 카탈로그를 완전히 벗어나도 실제 가격의 대안을 반환한다")
    void returnsAlternativesWithRealPrices() {
        List<AiGiftCandidate> candidates = client.recommendGifts(context(1_000_000, 2_000_000, List.of()));

        assertThat(candidates).isNotEmpty();
        assertPricesAreCatalogPrices(candidates);
        // 예산값(100만 원)을 가격으로 지어내지 않는다
        assertThat(candidates).allSatisfy(c -> assertThat(c.estimatedPriceMin()).isLessThan(1_000_000));
    }

    @Test
    @DisplayName("예산 내 후보를 우선하되 예산 밖 대안도 함께 제안한다")
    void prefersInBudgetButKeepsAlternatives() {
        List<AiGiftCandidate> candidates = client.recommendGifts(context(30_000, 70_000, List.of()));

        assertThat(candidates).hasSize(5);
        assertPricesAreCatalogPrices(candidates);

        long inBudget = candidates.stream()
                .filter(c -> c.estimatedPriceMin() <= 70_000 && c.estimatedPriceMax() >= 30_000)
                .count();
        assertThat(inBudget).as("예산 내 후보가 다수여야 한다").isGreaterThanOrEqualTo(3);
        assertThat(inBudget).as("예산 밖 대안도 최소 1건 포함되어야 한다").isLessThan(candidates.size());
    }

    @Test
    @DisplayName("제외 조건이 카탈로그를 모두 걸러내면 후보를 지어내지 않고 실패한다")
    void failsWhenEverythingExcluded() {
        List<AiPreviousGift> allGifts = GiftCatalog.ITEMS.stream()
                .map(item -> new AiPreviousGift(item.name(), item.category(), null))
                .toList();

        assertThatThrownBy(() -> client.recommendGifts(context(30_000, 70_000, allGifts)))
                .isInstanceOf(AiException.class)
                .hasMessageContaining("찾지 못했습니다");
    }
}
