package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.ai.*;
import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.*;
import com.skala.ikgeoljune.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비동기 AI 추천 처리에서 사용하는 DB 접근 계층.
 * AI 호출 자체는 트랜잭션 밖에서 수행하고, 이 클래스만 짧은 트랜잭션을 연다.
 */
@Service
@RequiredArgsConstructor
public class RecommendationStore {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;
    private final StructuredPreferenceRepository preferenceRepository;
    private final PreviousGiftRepository previousGiftRepository;
    private final FeedbackRepository feedbackRepository;

    @Value("${app.ai.candidate-count:5}")
    private int candidateCount;

    /** §8 AI 에 전달하는 정보를 모아 콘텍스트를 만든다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public AiRecommendationContext loadContext(Long recommendationId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ApiException(ErrorCode.RECOMMENDATION_NOT_FOUND));

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
        List<AiDislikedCandidate> disliked = List.of();
        Recommendation previous = recommendation.getPreviousRecommendation();
        if (previous != null) {
            disliked = feedbackRepository.findDislikesByRecommendationId(previous.getId()).stream()
                    .map(f -> new AiDislikedCandidate(
                            f.getCandidate().getGiftName(),
                            f.getCandidate().getGiftCategory(),
                            f.getDislikeReason()))
                    .toList();
        }

        return new AiRecommendationContext(
                recommendationId,
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

    /** AI 결과를 후보로 저장하고 status 를 SUCCESS 로 바꾼다. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccess(Long recommendationId, List<AiGiftCandidate> results) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ApiException(ErrorCode.RECOMMENDATION_NOT_FOUND));

        int rank = 1;
        for (AiGiftCandidate result : results) {
            candidateRepository.save(RecommendationCandidate.create(
                    recommendation,
                    result.giftName(),
                    result.giftCategory(),
                    result.estimatedPriceMin(),
                    result.estimatedPriceMax(),
                    result.recommendationReason(),
                    result.consideredInfo(),
                    result.cautionNote(),
                    rank++
            ));
        }
        recommendation.markSuccess();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long recommendationId) {
        recommendationRepository.findById(recommendationId).ifPresent(Recommendation::markFailed);
    }
}
