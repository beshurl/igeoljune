package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.feedback.FeedbackResponse;
import com.skala.ikgeoljune.dto.feedback.FeedbackUpsertRequest;
import com.skala.ikgeoljune.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** §9 피드백 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OwnershipValidator ownershipValidator;

    /**
     * FEEDBACK-001 등록·변경 (upsert).
     * candidate_id 가 유일하므로 기존 레코드가 있으면 새로 만들지 않고 수정한다.
     */
    @Transactional
    public FeedbackResponse upsert(Long candidateId, Long userId, FeedbackUpsertRequest request) {
        RecommendationCandidate candidate = ownershipValidator.getOwnedCandidate(candidateId, userId);

        Feedback feedback = feedbackRepository.findByCandidateId(candidateId)
                .map(existing -> {
                    existing.apply(request.feedbackType(), request.dislikeReason());
                    return existing;
                })
                .orElseGet(() -> feedbackRepository.save(
                        Feedback.create(candidate, request.feedbackType(), request.dislikeReason())));

        return FeedbackResponse.from(feedback);
    }

    /** FEEDBACK-002 — 피드백이 없으면 404 */
    public FeedbackResponse findOne(Long candidateId, Long userId) {
        ownershipValidator.getOwnedCandidate(candidateId, userId);
        return feedbackRepository.findByCandidateId(candidateId)
                .map(FeedbackResponse::from)
                .orElseThrow(() -> new ApiException(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    /** FEEDBACK-003 피드백 취소 */
    @Transactional
    public void delete(Long candidateId, Long userId) {
        ownershipValidator.getOwnedCandidate(candidateId, userId);
        Feedback feedback = feedbackRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEEDBACK_NOT_FOUND));
        feedbackRepository.delete(feedback);
    }
}
