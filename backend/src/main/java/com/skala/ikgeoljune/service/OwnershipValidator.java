package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.*;
import com.skala.ikgeoljune.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * §1.5 소유권 검증.
 * feedback → recommendation_candidates → recommendations → gift_conditions → recipient → users
 * 경로를 따라 로그인 사용자의 리소스인지 확인한다.
 *
 * <p>리소스가 없으면 404, 남의 리소스면 403 을 던진다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnershipValidator {

    private final RecipientRepository recipientRepository;
    private final StructuredPreferenceRepository preferenceRepository;
    private final PreviousGiftRepository previousGiftRepository;
    private final GiftConditionRepository giftConditionRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;

    public Recipient getOwnedRecipient(Long recipientId, Long userId) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "추천 대상을 찾을 수 없습니다."));
        checkOwner(recipient, userId);
        return recipient;
    }

    public StructuredPreference getOwnedPreference(Long preferenceId, Long userId) {
        StructuredPreference preference = preferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "취향 정보를 찾을 수 없습니다."));
        checkOwner(preference.getRecipient(), userId);
        return preference;
    }

    public PreviousGift getOwnedPreviousGift(Long previousGiftId, Long userId) {
        PreviousGift gift = previousGiftRepository.findById(previousGiftId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "과거 선물을 찾을 수 없습니다."));
        checkOwner(gift.getRecipient(), userId);
        return gift;
    }

    public GiftCondition getOwnedCondition(Long conditionId, Long userId) {
        GiftCondition condition = giftConditionRepository.findById(conditionId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "추천 조건을 찾을 수 없습니다."));
        checkOwner(condition.getRecipient(), userId);
        return condition;
    }

    public Recommendation getOwnedRecommendation(Long recommendationId, Long userId) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "추천 결과를 찾을 수 없습니다."));
        checkOwner(recommendation.getCondition().getRecipient(), userId);
        return recommendation;
    }

    public RecommendationCandidate getOwnedCandidate(Long candidateId, Long userId) {
        RecommendationCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "추천 후보를 찾을 수 없습니다."));
        checkOwner(candidate.getRecommendation().getCondition().getRecipient(), userId);
        return candidate;
    }

    private void checkOwner(Recipient recipient, Long userId) {
        if (!recipient.isOwnedBy(userId)) {
            throw new ApiException(ErrorCode.RESOURCE_FORBIDDEN);
        }
    }
}
