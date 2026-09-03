package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * USER-003 / RECIPIENT-005 하위 데이터 일괄 삭제.
 * recommendations 의 자기참조(previous_recommendation_id) 때문에 링크를 먼저 끊고 삭제한다.
 */
@Service
@RequiredArgsConstructor
public class CascadeDeleteService {

    private final RecipientRepository recipientRepository;
    private final StructuredPreferenceRepository preferenceRepository;
    private final PreviousGiftRepository previousGiftRepository;
    private final GiftConditionRepository giftConditionRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void deleteRecipients(List<Long> recipientIds) {
        if (recipientIds.isEmpty()) {
            return;
        }

        List<Long> conditionIds = giftConditionRepository.findIdsByRecipientIds(recipientIds);
        if (!conditionIds.isEmpty()) {
            List<Long> recommendationIds = recommendationRepository.findIdsByConditionIds(conditionIds);
            deleteRecommendations(recommendationIds);
            giftConditionRepository.deleteByIds(conditionIds);
        }

        previousGiftRepository.deleteByRecipientIds(recipientIds);
        preferenceRepository.deleteByRecipientIds(recipientIds);
        recipientRepository.deleteByIds(recipientIds);
    }

    @Transactional
    public void deleteRecommendations(List<Long> recommendationIds) {
        if (recommendationIds.isEmpty()) {
            return;
        }
        feedbackRepository.deleteByRecommendationIds(recommendationIds);
        candidateRepository.deleteByRecommendationIds(recommendationIds);
        recommendationRepository.clearPreviousLinks(recommendationIds);
        recommendationRepository.deleteByIds(recommendationIds);
    }
}
