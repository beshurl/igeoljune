package com.skala.ikgeoljune.dto.feedback;

import com.skala.ikgeoljune.domain.DislikeReason;
import com.skala.ikgeoljune.domain.Feedback;
import com.skala.ikgeoljune.domain.FeedbackType;

import java.time.OffsetDateTime;

/** FEEDBACK-001 / FEEDBACK-002 응답 */
public record FeedbackResponse(
        Long feedbackId,
        Long candidateId,
        FeedbackType feedbackType,
        DislikeReason dislikeReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getCandidate().getId(),
                feedback.getFeedbackType(),
                feedback.getDislikeReason(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}
