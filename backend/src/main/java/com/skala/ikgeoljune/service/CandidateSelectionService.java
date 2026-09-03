package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.recommendation.RecommendationCandidateResponse;
import com.skala.ikgeoljune.repository.FeedbackRepository;
import com.skala.ikgeoljune.repository.RecommendationCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 최종 선물 선택.
 *
 * <p>추천 후보 하나를 최종 선물로 선택하고 {@code selectedAt} 을 기록한다.
 * 한 추천 실행 안에서는 최대 1건만 선택할 수 있어, 다른 후보를 선택하면 기존 선택은 해제된다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateSelectionService {

    private final RecommendationCandidateRepository candidateRepository;
    private final FeedbackRepository feedbackRepository;
    private final OwnershipValidator ownershipValidator;

    /** 최종 선물 선택 */
    @Transactional
    public RecommendationCandidateResponse select(Long candidateId, Long userId) {
        RecommendationCandidate candidate = ownershipValidator.getOwnedCandidate(candidateId, userId);

        Long recommendationId = candidate.getRecommendation().getId();
        List<RecommendationCandidate> alreadySelected =
                candidateRepository.findByRecommendationIdAndSelectedAtIsNotNull(recommendationId);

        for (RecommendationCandidate selected : alreadySelected) {
            if (!selected.getId().equals(candidateId)) {
                selected.deselect();
            }
        }
        // 유니크 인덱스 충돌을 피하려고 기존 선택 해제를 먼저 반영한다.
        candidateRepository.flush();

        if (!candidate.isSelected()) {
            candidate.select();
        }
        return toResponse(candidate);
    }

    /** 선택 취소 */
    @Transactional
    public void deselect(Long candidateId, Long userId) {
        ownershipValidator.getOwnedCandidate(candidateId, userId).deselect();
    }

    /** 선택 상태 조회 */
    public RecommendationCandidateResponse findOne(Long candidateId, Long userId) {
        return toResponse(ownershipValidator.getOwnedCandidate(candidateId, userId));
    }

    private RecommendationCandidateResponse toResponse(RecommendationCandidate candidate) {
        return RecommendationCandidateResponse.of(
                candidate, feedbackRepository.findByCandidateId(candidate.getId()).orElse(null));
    }
}
