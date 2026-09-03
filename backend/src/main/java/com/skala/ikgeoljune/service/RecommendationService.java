package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.*;
import com.skala.ikgeoljune.dto.recommendation.RecommendationCandidateResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationDetailResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationResponse;
import com.skala.ikgeoljune.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 추천·재추천.
 *
 * <p>MVP 계약: Mock 추천은 요청 스레드 안에서 실행하고 완료된 후보를 201 로 즉시 반환한다.
 * 프론트가 PROCESSING 을 폴링하지 않는다.
 * 실제 LLM 연동으로 처리 시간이 길어지면 그때 비동기 + 폴링으로 전환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;
    private final StructuredPreferenceRepository preferenceRepository;
    private final PreviousGiftRepository previousGiftRepository;
    private final FeedbackRepository feedbackRepository;
    private final OwnershipValidator ownershipValidator;
    private final GiftAiClient giftAiClient;

    @Value("${app.ai.candidate-count:5}")
    private int candidateCount;

    /** RECOMMEND-001 AI 추천 요청 — 201 Created, 후보 포함 */
    @Transactional
    public RecommendationDetailResponse request(Long conditionId, Long userId) {
        GiftCondition condition = ownershipValidator.getOwnedCondition(conditionId, userId);
        return execute(Recommendation.start(condition), null);
    }

    /** RECOMMEND-004 피드백 반영 재추천 — 201 Created, 후보 포함 */
    @Transactional
    public RecommendationDetailResponse reRecommend(Long recommendationId, Long userId) {
        Recommendation previous = ownershipValidator.getOwnedRecommendation(recommendationId, userId);
        return execute(Recommendation.reRecommend(previous.getCondition(), previous), previous);
    }

    private RecommendationDetailResponse execute(Recommendation recommendation, Recommendation previous) {
        Recommendation saved = recommendationRepository.save(recommendation);

        List<AiGiftCandidate> results;
        try {
            results = giftAiClient.recommendGifts(buildContext(saved, previous));
        } catch (AiException e) {
            // 후보를 만들지 못하면 실행 기록도 남기지 않고 422 로 알린다.
            log.warn("AI 추천 실패 - conditionId={}", saved.getCondition().getId(), e);
            throw new ApiException(ErrorCode.AI_RESULT_INVALID, e.getMessage());
        }

        int rank = 1;
        List<RecommendationCandidate> candidates = new ArrayList<>();
        for (AiGiftCandidate result : results) {
            candidates.add(candidateRepository.save(RecommendationCandidate.create(
                    saved,
                    result.giftName(),
                    result.giftCategory(),
                    result.estimatedPriceMin(),
                    result.estimatedPriceMax(),
                    result.recommendationReason(),
                    result.consideredInfo(),
                    result.cautionNote(),
                    rank++
            )));
        }
        saved.markSuccess();

        List<RecommendationCandidateResponse> items = candidates.stream()
                .map(candidate -> RecommendationCandidateResponse.of(candidate, null))
                .toList();
        return RecommendationDetailResponse.of(saved, items);
    }

    /** RECOMMEND-002 추천 결과 조회 */
    public RecommendationDetailResponse findOne(Long recommendationId, Long userId) {
        Recommendation recommendation = ownershipValidator.getOwnedRecommendation(recommendationId, userId);

        List<RecommendationCandidate> candidates =
                candidateRepository.findByRecommendationIdOrderByRecommendRankAsc(recommendationId);

        Map<Long, Feedback> feedbackByCandidateId = candidates.isEmpty()
                ? Map.of()
                : feedbackRepository.findByCandidateIdIn(candidates.stream().map(RecommendationCandidate::getId).toList())
                .stream()
                .collect(Collectors.toMap(f -> f.getCandidate().getId(), Function.identity()));

        List<RecommendationCandidateResponse> items = candidates.stream()
                .map(candidate -> RecommendationCandidateResponse.of(
                        candidate, feedbackByCandidateId.get(candidate.getId())))
                .toList();

        return RecommendationDetailResponse.of(recommendation, items);
    }

    /** RECOMMEND-003 조건별 추천 목록 — 생성일 역순 */
    public ListResponse<RecommendationResponse> findAllByCondition(Long conditionId, Long userId) {
        ownershipValidator.getOwnedCondition(conditionId, userId);
        List<RecommendationResponse> items =
                recommendationRepository.findByConditionIdOrderByCreatedAtDescIdDesc(conditionId).stream()
                        .map(RecommendationResponse::from)
                        .toList();
        return ListResponse.of(items);
    }

    /** AI 에 전달하는 정보: recipient / structured_preference / previous_gifts / gift_conditions */
    private AiRecommendationContext buildContext(Recommendation recommendation, Recommendation previous) {
        GiftCondition condition = recommendation.getCondition();
        Recipient recipient = condition.getRecipient();

        List<AiPreference> preferences = preferenceRepository
                .findByRecipientIdOrderByIdAsc(recipient.getId()).stream()
                .map(p -> new AiPreference(p.getPreferenceType(), p.getPreferenceValue(), p.getSourceType()))
                .toList();

        List<AiPreviousGift> previousGifts = previousGiftRepository
                .findByRecipientIdOrderByGiftedAtDescIdDesc(recipient.getId()).stream()
                .map(g -> new AiPreviousGift(g.getGiftName(), g.getGiftCategory(), g.getGiftedAt()))
                .toList();

        // RECOMMEND-004: 이전 추천에서 DISLIKE 로 등록된 후보의 상품명과 사유를 콘텍스트에 포함한다.
        List<AiDislikedCandidate> disliked = previous == null ? List.of()
                : feedbackRepository.findDislikesByRecommendationId(previous.getId()).stream()
                .map(f -> new AiDislikedCandidate(
                        f.getCandidate().getGiftName(),
                        f.getCandidate().getGiftCategory(),
                        f.getDislikeReason()))
                .toList();

        return new AiRecommendationContext(
                recommendation.getId(),
                new AiRecipientProfile(recipient.getName(), recipient.getRelationship(),
                        recipient.getAgeGroup(), recipient.getGender(), recipient.getJob()),
                preferences,
                previousGifts,
                new AiGiftConditionSpec(condition.getBudgetMin(), condition.getBudgetMax(),
                        condition.getOccasionType(), condition.getOccasionDate(),
                        condition.getPreferenceNote(), condition.getAvoidGiftNote()),
                disliked,
                candidateCount
        );
    }
}
