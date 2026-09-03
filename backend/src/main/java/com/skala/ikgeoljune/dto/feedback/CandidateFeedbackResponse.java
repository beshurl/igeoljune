package com.skala.ikgeoljune.dto.feedback;

import com.skala.ikgeoljune.domain.DislikeReason;
import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.FeedbackType;

/** RECOMMEND-002 추천 후보 안에 중첩되는 피드백 (없으면 null) */
public record CandidateFeedbackResponse(
        Long feedbackId,
        FeedbackType feedbackType,
        DislikeReason dislikeReason
) {
    public static CandidateFeedbackResponse from(Feedback feedback) {
        return new CandidateFeedbackResponse(
                feedback.getId(), feedback.getFeedbackType(), feedback.getDislikeReason());
    }
}
