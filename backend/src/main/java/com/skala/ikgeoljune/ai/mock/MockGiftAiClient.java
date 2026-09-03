package com.skala.ikgeoljune.ai.mock;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.domain.PreferenceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Mock AI 구현.
 *
 * <p>실제 LLM 호출 없이 카탈로그 기반 규칙으로 §8 추천과 KAKAO-001 분석 결과를 만들어 낸다.
 * 입력 콘텍스트(취향·과거 선물·예산·재추천 DISLIKE 사유)를 실제로 반영하므로
 * 프론트엔드는 실제 AI 연동과 동일한 흐름으로 개발할 수 있다.
 *
 * <p>실제 모델로 교체할 때는 {@link GiftAiClient} 를 새로 구현하고
 * {@code app.ai.provider} 값을 mock 이외의 값으로 바꾸면 된다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockGiftAiClient implements GiftAiClient {

    /** 카카오톡 대화에서 관심사를 찾아내는 규칙: 키워드 → (취향 유형, 저장할 값) */
    private static final List<KeywordRule> INTEREST_RULES = List.of(
            new KeywordRule(List.of("홈카페", "드립", "원두", "에스프레소"), PreferenceType.INTEREST, "홈카페"),
            new KeywordRule(List.of("커피", "아메리카노", "라떼"), PreferenceType.INTEREST, "커피"),
            new KeywordRule(List.of("러닝", "달리기", "마라톤", "조깅"), PreferenceType.INTEREST, "러닝"),
            new KeywordRule(List.of("헬스", "운동", "필라테스", "요가"), PreferenceType.INTEREST, "운동"),
            new KeywordRule(List.of("독서", "책", "소설", "에세이"), PreferenceType.INTEREST, "독서"),
            new KeywordRule(List.of("캠핑", "등산", "백패킹"), PreferenceType.INTEREST, "아웃도어"),
            new KeywordRule(List.of("게임", "보드게임", "닌텐도"), PreferenceType.INTEREST, "게임"),
            new KeywordRule(List.of("그림", "드로잉", "아이패드로 그림"), PreferenceType.INTEREST, "드로잉"),
            new KeywordRule(List.of("반려견", "강아지", "고양이", "댕댕이", "냥이"), PreferenceType.INTEREST, "반려동물"),
            new KeywordRule(List.of("베이킹", "빵", "디저트", "케이크"), PreferenceType.INTEREST, "디저트"),
            new KeywordRule(List.of("여행", "호캉스", "비행기"), PreferenceType.INTEREST, "여행"),
            new KeywordRule(List.of("미니멀", "심플한", "깔끔한"), PreferenceType.PREFERRED_ATTRIBUTE, "미니멀 디자인"),
            new KeywordRule(List.of("실용적", "쓸모", "잘 쓰는"), PreferenceType.PREFERRED_ATTRIBUTE, "실용성"),
            new KeywordRule(List.of("파스텔", "귀여운", "아기자기"), PreferenceType.PREFERRED_ATTRIBUTE, "아기자기한 디자인")
    );

    /** 갖고 싶다는 표현이 함께 등장하면 WISH_ITEM 으로 승격시키는 품목 키워드 */
    private static final List<KeywordRule> WISH_RULES = List.of(
            new KeywordRule(List.of("무선충전", "무선 충전"), PreferenceType.WISH_ITEM, "무선 충전기"),
            new KeywordRule(List.of("키보드"), PreferenceType.WISH_ITEM, "기계식 키보드"),
            new KeywordRule(List.of("이어폰", "에어팟"), PreferenceType.WISH_ITEM, "무선 이어폰"),
            new KeywordRule(List.of("텀블러"), PreferenceType.WISH_ITEM, "텀블러"),
            new KeywordRule(List.of("러닝화", "운동화"), PreferenceType.WISH_ITEM, "러닝화"),
            new KeywordRule(List.of("디퓨저", "캔들"), PreferenceType.WISH_ITEM, "디퓨저"),
            new KeywordRule(List.of("전자책", "크레마", "킨들"), PreferenceType.WISH_ITEM, "전자책 리더기")
    );

    private static final List<String> WISH_MARKERS =
            List.of("갖고 싶", "가지고 싶", "사고 싶", "사고싶", "필요해", "필요한데", "위시", "지르고 싶");

    private static final List<String> DISLIKE_MARKERS =
            List.of("싫어", "별로", "안 좋아", "안좋아", "부담", "취향 아니", "취향아니");

    /** 비선호 표현과 함께 등장하면 DISLIKED_CATEGORY 로 저장할 키워드 */
    private static final List<KeywordRule> DISLIKE_RULES = List.of(
            new KeywordRule(List.of("향수"), PreferenceType.DISLIKED_CATEGORY, "향수"),
            new KeywordRule(List.of("화장품", "코스메틱"), PreferenceType.DISLIKED_CATEGORY, "화장품"),
            new KeywordRule(List.of("옷", "의류", "패션"), PreferenceType.DISLIKED_CATEGORY, "의류"),
            new KeywordRule(List.of("액세서리", "귀걸이", "목걸이"), PreferenceType.DISLIKED_CATEGORY, "액세서리"),
            new KeywordRule(List.of("술", "와인", "위스키"), PreferenceType.DISLIKED_CATEGORY, "주류"),
            new KeywordRule(List.of("케이크", "디저트"), PreferenceType.DISLIKED_CATEGORY, "디저트"),
            new KeywordRule(List.of("전자기기", "가전"), PreferenceType.DISLIKED_CATEGORY, "전자기기")
    );

    /** 예산 내 후보가 충분해도 항상 끼워 넣는 예산 밖 대안 개수 */
    private static final int MAX_ALTERNATIVES = 1;

    // ------------------------------------------------------------------
    // KAKAO-001
    // ------------------------------------------------------------------
    @Override
    public List<AiExtractedPreference> extractPreferences(AiKakaoAnalysisContext context) {
        String text = context.chatText();
        if (!StringUtils.hasText(text)) {
            throw new AiException("분석할 대화 내용이 비어 있습니다.");
        }
        String normalized = text.toLowerCase(Locale.KOREAN);
        Map<String, AiExtractedPreference> found = new LinkedHashMap<>();

        for (KeywordRule rule : INTEREST_RULES) {
            if (rule.matches(normalized)) {
                putIfAbsent(found, rule.type(), rule.value());
            }
        }
        for (String line : text.split("\\R")) {
            String lower = line.toLowerCase(Locale.KOREAN);
            if (containsAny(lower, WISH_MARKERS)) {
                WISH_RULES.stream()
                        .filter(rule -> rule.matches(lower))
                        .forEach(rule -> putIfAbsent(found, rule.type(), rule.value()));
            }
            if (containsAny(lower, DISLIKE_MARKERS)) {
                DISLIKE_RULES.stream()
                        .filter(rule -> rule.matches(lower))
                        .forEach(rule -> putIfAbsent(found, rule.type(), rule.value()));
            }
        }

        List<AiExtractedPreference> result = new ArrayList<>(found.values());
        log.debug("[MockAI] 카카오톡 분석 결과 {}건", result.size());
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    // ------------------------------------------------------------------
    // RECOMMEND-001 / RECOMMEND-004
    // ------------------------------------------------------------------
    @Override
    public List<AiGiftCandidate> recommendGifts(AiRecommendationContext context) {
        Exclusions exclusions = Exclusions.from(context);
        List<String> positiveKeywords = positiveKeywords(context);

        List<Scored> inBudget = new ArrayList<>();
        List<Scored> alternatives = new ArrayList<>();
        for (GiftCatalog.Item item : GiftCatalog.ITEMS) {
            if (exclusions.isExcluded(item)) {
                continue;
            }
            List<String> matched = matchedKeywords(item, positiveKeywords);
            Scored s = new Scored(item, score(context, item, matched), matched);
            if (overlapsBudget(item, context.condition())) {
                inBudget.add(s);
            } else {
                alternatives.add(s);
            }
        }

        Comparator<Scored> byScore = Comparator.comparingInt(Scored::score).reversed()
                .thenComparing(s -> s.item().name());
        inBudget.sort(byScore);
        // 대안은 예산에서 가장 덜 벗어난 순서로 제안한다.
        alternatives.sort(Comparator
                .comparingInt((Scored s) -> budgetGap(s.item(), context.condition()))
                .thenComparing(byScore));

        List<Scored> picked = pick(inBudget, alternatives, context.candidateCount());
        if (picked.isEmpty()) {
            throw new AiException("제외 조건을 만족하는 선물 후보를 찾지 못했습니다.");
        }

        List<AiGiftCandidate> candidates = new ArrayList<>();
        for (Scored s : picked) {
            boolean outOfBudget = !overlapsBudget(s.item(), context.condition());
            candidates.add(new AiGiftCandidate(
                    s.item().name(),
                    s.item().category(),
                    // 예산으로 보정하지 않고 카탈로그 실제 가격을 그대로 사용한다.
                    s.item().priceMin(),
                    s.item().priceMax(),
                    buildReason(context, s, outOfBudget),
                    buildConsideredInfo(context, s, outOfBudget),
                    s.item().cautionNote()
            ));
        }
        return candidates;
    }

    /**
     * 예산 내 후보를 우선하되, 기획의 "예산보다 낮거나 높은 대안 제공"을 위해
     * 예산 밖 후보도 최대 {@value #MAX_ALTERNATIVES} 건까지 섞어 반환한다.
     */
    private List<Scored> pick(List<Scored> inBudget, List<Scored> alternatives, int count) {
        int alternativeQuota = Math.min(alternatives.size(),
                Math.max(MAX_ALTERNATIVES, count - inBudget.size()));
        int inBudgetQuota = Math.min(inBudget.size(), count - alternativeQuota);
        // 예산 내 후보가 부족하면 대안으로 남은 자리를 채운다.
        alternativeQuota = Math.min(alternatives.size(), count - inBudgetQuota);

        List<Scored> picked = new ArrayList<>(inBudget.subList(0, inBudgetQuota));
        picked.addAll(alternatives.subList(0, alternativeQuota));
        return picked;
    }

    /** 예산에서 벗어난 정도(원). 예산 안이면 0 */
    private int budgetGap(GiftCatalog.Item item, AiGiftConditionSpec condition) {
        if (item.priceMax() < condition.budgetMin()) {
            return condition.budgetMin() - item.priceMax();
        }
        if (item.priceMin() > condition.budgetMax()) {
            return item.priceMin() - condition.budgetMax();
        }
        return 0;
    }

    // ------------------------------------------------------------------
    // 점수 계산
    // ------------------------------------------------------------------
    private int score(AiRecommendationContext context, GiftCatalog.Item item, List<String> matched) {
        int score = matched.size() * 6;

        // 이 시점의 후보는 모두 예산과 겹친다. 예산 중앙값에 가까울수록 가점.
        AiGiftConditionSpec condition = context.condition();
        int budgetCenter = (condition.budgetMin() + condition.budgetMax()) / 2;
        int itemCenter = (item.priceMin() + item.priceMax()) / 2;
        score += Math.max(0, 6 - Math.abs(budgetCenter - itemCenter) / 20000);

        // WISH_ITEM 은 가장 강한 신호로 취급한다.
        for (AiPreference preference : context.preferences()) {
            if (preference.preferenceType() == PreferenceType.WISH_ITEM
                    && matchesToken(item, preference.preferenceValue())) {
                score += 10;
            }
        }

        if (matchesOccasion(item, condition.occasionType())) {
            score += 4;
        }
        if (containsKeyword(condition.preferenceNote(), item)) {
            score += 5;
        }

        // 같은 조건이라도 재추천마다 결과가 달라지도록 하는 결정적 흔들림
        score += Math.floorMod(Objects.hash(context.seed(), item.name()), 4);
        return score;
    }

    /** 상품 가격대와 예산이 겹치는지 */
    private boolean overlapsBudget(GiftCatalog.Item item, AiGiftConditionSpec condition) {
        return item.priceMin() <= condition.budgetMax() && item.priceMax() >= condition.budgetMin();
    }

    private List<String> positiveKeywords(AiRecommendationContext context) {
        List<String> keywords = new ArrayList<>();
        for (AiPreference preference : context.preferences()) {
            if (preference.preferenceType() != PreferenceType.DISLIKED_CATEGORY) {
                keywords.add(preference.preferenceValue());
            }
        }
        if (StringUtils.hasText(context.recipient().job())) {
            keywords.add(context.recipient().job());
        }
        return keywords;
    }

    private List<String> matchedKeywords(GiftCatalog.Item item, List<String> keywords) {
        List<String> matched = new ArrayList<>();
        for (String keyword : keywords) {
            if (matchesToken(item, keyword)) {
                matched.add(keyword);
            }
        }
        return matched;
    }

    private boolean matchesToken(GiftCatalog.Item item, String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return false;
        }
        String token = rawToken.toLowerCase(Locale.KOREAN).trim();
        if (item.name().toLowerCase(Locale.KOREAN).contains(token)) {
            return true;
        }
        return item.tags().stream()
                .map(tag -> tag.toLowerCase(Locale.KOREAN))
                .anyMatch(tag -> tag.contains(token) || token.contains(tag));
    }

    private boolean matchesOccasion(GiftCatalog.Item item, String occasionType) {
        if (!StringUtils.hasText(occasionType)) {
            return false;
        }
        String occasion = occasionType.toUpperCase(Locale.ROOT);
        return switch (occasion) {
            case "BIRTHDAY" -> item.tags().contains("생일") || item.tags().contains("디저트");
            case "ANNIVERSARY", "WEDDING" -> item.tags().contains("기념일") || item.tags().contains("인테리어");
            case "THANKS", "PARENTS_DAY" -> item.tags().contains("건강") || item.tags().contains("부모님");
            case "PROMOTION", "GRADUATION", "EMPLOYMENT" -> item.tags().contains("작업환경") || item.tags().contains("책상");
            default -> false;
        };
    }

    private boolean containsKeyword(String note, GiftCatalog.Item item) {
        if (!StringUtils.hasText(note)) {
            return false;
        }
        String lower = note.toLowerCase(Locale.KOREAN);
        return item.tags().stream().anyMatch(tag -> lower.contains(tag.toLowerCase(Locale.KOREAN)));
    }

    // ------------------------------------------------------------------
    // 문구 생성
    // ------------------------------------------------------------------
    private String buildReason(AiRecommendationContext context, Scored scored, boolean outOfBudget) {
        StringBuilder sb = new StringBuilder();
        String name = context.recipient().name();

        if (!scored.matched().isEmpty()) {
            sb.append(String.join(", ", scored.matched()))
                    .append(" 취향에 맞춰 골랐습니다. ");
        } else {
            sb.append(name).append("님의 ")
                    .append(context.recipient().ageGroup())
                    .append(" ")
                    .append(context.recipient().relationship())
                    .append(" 관계에서 무난하게 받아들여지는 선물입니다. ");
        }

        sb.append(context.condition().occasionType()).append(" 상황을 고려했습니다. ");
        if (outOfBudget) {
            sb.append("요청하신 ").append(formatBudget(context.condition()))
                    .append(" 예산을 벗어나지만 함께 검토해 볼 만한 대안입니다.");
        } else {
            sb.append(formatBudget(context.condition())).append(" 예산에 맞는 가격대입니다.");
        }

        if (context.isReRecommendation()) {
            sb.append(" 이전 추천에서 마음에 들지 않았던 항목은 제외하고 다시 골랐습니다.");
        }
        return sb.toString();
    }

    private String buildConsideredInfo(AiRecommendationContext context, Scored scored, boolean outOfBudget) {
        List<String> parts = new ArrayList<>();
        if (!scored.matched().isEmpty()) {
            parts.add(String.join(", ", scored.matched()));
        }
        parts.add(context.condition().occasionType());
        parts.add(formatBudget(context.condition()) + (outOfBudget ? " 예산 외 대안" : " 예산 내"));
        if (!context.previousGifts().isEmpty()) {
            parts.add("과거 선물 " + context.previousGifts().size() + "건 중복 회피");
        }
        if (context.isReRecommendation()) {
            String reasons = context.dislikedCandidates().stream()
                    .filter(d -> d.dislikeReason() != null)
                    .map(d -> d.dislikeReason().description())
                    .distinct()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            parts.add(reasons.isEmpty() ? "이전 비선호 후보 제외" : "이전 비선호 사유(" + reasons + ") 반영");
        }
        return String.join(", ", parts);
    }

    private String formatBudget(AiGiftConditionSpec condition) {
        return String.format("%,d~%,d원", condition.budgetMin(), condition.budgetMax());
    }

    // ------------------------------------------------------------------
    // 보조 타입
    // ------------------------------------------------------------------
    private record Scored(GiftCatalog.Item item, int score, List<String> matched) {
    }

    private record KeywordRule(List<String> keywords, PreferenceType type, String value) {
        boolean matches(String lowerText) {
            return keywords.stream().anyMatch(k -> lowerText.contains(k.toLowerCase(Locale.KOREAN)));
        }
    }

    /** 추천에서 제외할 상품명·카테고리 집합 */
    private record Exclusions(Set<String> giftNames, Set<String> categories, List<String> avoidKeywords) {

        static Exclusions from(AiRecommendationContext context) {
            Set<String> names = new HashSet<>();
            Set<String> categories = new HashSet<>();

            // 과거에 이미 준 선물은 제외한다.
            context.previousGifts().forEach(gift -> names.add(normalize(gift.giftName())));

            // 이전 추천에서 DISLIKE 한 후보는 제외한다. (RECOMMEND-004)
            context.dislikedCandidates().forEach(disliked -> {
                names.add(normalize(disliked.giftName()));
                if (disliked.dislikeReason() != null) {
                    switch (disliked.dislikeReason()) {
                        case TASTE_MISMATCH, WANT_DIFFERENT_STYLE -> categories.add(normalize(disliked.giftCategory()));
                        default -> {
                        }
                    }
                }
            });

            // DISLIKED_CATEGORY 취향은 카테고리·키워드 양쪽으로 제외한다.
            List<String> avoid = new ArrayList<>();
            context.preferences().stream()
                    .filter(p -> p.preferenceType() == PreferenceType.DISLIKED_CATEGORY)
                    .forEach(p -> avoid.add(normalize(p.preferenceValue())));

            if (StringUtils.hasText(context.condition().avoidGiftNote())) {
                for (String token : context.condition().avoidGiftNote().split("[,\\s]+")) {
                    String normalized = normalize(token);
                    if (normalized.length() >= 2) {
                        avoid.add(normalized);
                    }
                }
            }
            return new Exclusions(names, categories, avoid);
        }

        boolean isExcluded(GiftCatalog.Item item) {
            if (giftNames.contains(normalize(item.name()))) {
                return true;
            }
            if (categories.contains(normalize(item.category()))) {
                return true;
            }
            String name = normalize(item.name());
            String category = normalize(item.category());
            for (String keyword : avoidKeywords) {
                if (keyword.isBlank()) {
                    continue;
                }
                if (name.contains(keyword) || category.contains(keyword)) {
                    return true;
                }
                boolean tagHit = item.tags().stream()
                        .map(Exclusions::normalize)
                        .anyMatch(tag -> tag.contains(keyword) || keyword.contains(tag));
                if (tagHit) {
                    return true;
                }
            }
            return false;
        }

        private static String normalize(String value) {
            return value == null ? "" : value.toLowerCase(Locale.KOREAN).replaceAll("\\s+", "");
        }
    }

    private static void putIfAbsent(Map<String, AiExtractedPreference> found, PreferenceType type, String value) {
        found.putIfAbsent(type.name() + "|" + value, new AiExtractedPreference(type, value));
    }

    private static boolean containsAny(String text, List<String> markers) {
        return markers.stream().anyMatch(text::contains);
    }

}
