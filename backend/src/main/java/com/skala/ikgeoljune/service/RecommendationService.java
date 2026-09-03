package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.GiftCondition;
import com.skala.ikgeoljune.domain.Recommendation;
import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.recommendation.RecommendationCandidateResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationDetailResponse;
import com.skala.ikgeoljune.dto.recommendation.RecommendationResponse;
import com.skala.ikgeoljune.repository.FeedbackRepository;
import com.skala.ikgeoljune.repository.RecommendationCandidateRepository;
import com.skala.ikgeoljune.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** §8 AI 추천·재추천 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationCandidateRepository candidateRepository;
    private final FeedbackRepository feedbackRepository;
    private final OwnershipValidator ownershipValidator;
    private final ApplicationEventPublisher eventPublisher;

    /** RECOMMEND-001 AI 추천 요청 — 202 Accepted, status=PROCESSING */
    @Transactional
    public RecommendationResponse request(Long conditionId, Long userId) {
        GiftCondition condition = ownershipValidator.getOwnedCondition(conditionId, userId);
        Recommendation recommendation = recommendationRepository.save(Recommendation.start(condition));
        eventPublisher.publishEvent(new RecommendationRequestedEvent(recommendation.getId()));
        return RecommendationResponse.from(recommendation);
    }

    /** RECOMMEND-004 피드백 반영 재추천 — 새 레코드를 만들고 이전 추천을 연결한다. */
    @Transactional
    public RecommendationResponse reRecommend(Long recommendationId, Long userId) {
        Recommendation previous = ownershipValidator.getOwnedRecommendation(recommendationId, userId);
        Recommendation recommendation = recommendationRepository.save(
                Recommendation.reRecommend(previous.getCondition(), previous));
        eventPublisher.publishEvent(new RecommendationRequestedEvent(recommendation.getId()));
        return RecommendationResponse.from(recommendation);
    }

    /**
     * RECOMMEND-002 추천 결과 조회.
     * PROCESSING·FAILED 이면 candidates 는 빈 배열이다.
     */
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
}
