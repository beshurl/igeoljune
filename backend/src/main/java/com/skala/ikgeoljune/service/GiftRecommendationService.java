package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.*;
import com.skala.ikgeoljune.dto.request.FeedbackRequest;
import com.skala.ikgeoljune.dto.request.GiftConditionRequest;
import com.skala.ikgeoljune.dto.response.RecommendationResponse;
import com.skala.ikgeoljune.exception.NotFoundException;
import com.skala.ikgeoljune.repository.GiftConditionRepository;
import com.skala.ikgeoljune.repository.RecipientRepository;
import com.skala.ikgeoljune.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 대표 흐름 전담 서비스: UC7(조건 입력) -> UC8(AI 추천 요청) -> UC9(결과 확인)
 * -> UC10/UC11(피드백) -> UC12(재추천) -> UC13(확정)
 */
@Service
@RequiredArgsConstructor
public class GiftRecommendationService {

    private final RecipientRepository recipientRepository;
    private final GiftConditionRepository giftConditionRepository;
    private final RecommendationRepository recommendationRepository;
    private final AiGiftAdvisor aiGiftAdvisor;

    // SCR-GIFT-001 · UC7
    @Transactional
    public Long createCondition(Long recipientId, GiftConditionRequest request) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("대상을 찾을 수 없습니다: " + recipientId));

        GiftCondition condition = GiftCondition.builder()
                .recipient(recipient)
                .budget(request.getBudget())
                .anniversaryDate(request.getAnniversaryDate())
                .preferenceTags(String.join(",", request.getPreferenceTags() == null ? List.<String>of() : request.getPreferenceTags()))
                .excludeTags(String.join(",", request.getExcludeTags() == null ? List.<String>of() : request.getExcludeTags()))
                .build();

        return giftConditionRepository.save(condition).getId();
    }

    // SCR-GIFT-001 -> SCR-AI-001 · UC8 (AI 확장 지점) -> UC9
    @Transactional
    public RecommendationResponse requestRecommendation(Long giftConditionId) {
        GiftCondition condition = giftConditionRepository.findById(giftConditionId)
                .orElseThrow(() -> new NotFoundException("추천 조건을 찾을 수 없습니다: " + giftConditionId));

        Recommendation recommendation = Recommendation.builder()
                .giftCondition(condition)
                .status(RecommendationStatus.PENDING)
                .build();
        final Recommendation savedRecommendation = recommendationRepository.save(recommendation);
        recommendation = savedRecommendation;

        try {
            List<AiGiftAdvisor.GiftCandidateAi> aiCandidates = aiGiftAdvisor.recommend(condition);

            List<RecommendationCandidate> candidates = aiCandidates.stream()
                    .map(c -> RecommendationCandidate.builder()
                            .recommendation(savedRecommendation)
                            .name(c.name())
                            .price(c.price())
                            .reason(c.reason())
                            .build())
                    .collect(Collectors.toList());

            recommendation.getCandidates().addAll(candidates);
            recommendation.setStatus(RecommendationStatus.COMPLETED);
        } catch (Exception e) {
            recommendation.setStatus(RecommendationStatus.FAILED);
            recommendation.setFailureReason(e.getMessage());
        }

        return RecommendationResponse.from(recommendation);
    }

    // SCR-AI-001 · UC9
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendation(Long recommendationId) {
        return RecommendationResponse.from(findRecommendation(recommendationId));
    }

    // SCR-AI-002 · UC10·UC11
    @Transactional
    public void submitFeedback(Long recommendationId, FeedbackRequest request) {
        Recommendation recommendation = findRecommendation(recommendationId);
        recommendation.getCandidates().stream()
                .filter(c -> c.getId().equals(request.getCandidateId()))
                .findFirst()
                .ifPresent(c -> {
                    c.setLiked(request.isLiked());
                    c.setDislikeReason(request.getDislikeReason());
                });
    }

    // SCR-AI-002 · UC12 재추천 (이전 추천을 참조하는 새 추천 생성)
    @Transactional
    public RecommendationResponse reRecommend(Long recommendationId) {
        Recommendation previous = findRecommendation(recommendationId);
        RecommendationResponse fresh = requestRecommendation(previous.getGiftCondition().getId());

        Recommendation newRecommendation = findRecommendation(fresh.getId());
        newRecommendation.setPreviousRecommendationId(previous.getId());

        return RecommendationResponse.from(newRecommendation);
    }

    private Recommendation findRecommendation(Long id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("추천 결과를 찾을 수 없습니다: " + id));
    }
}
