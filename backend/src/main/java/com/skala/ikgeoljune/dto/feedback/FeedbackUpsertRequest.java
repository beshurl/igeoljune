package com.skala.ikgeoljune.dto.feedback;

import com.skala.ikgeoljune.domain.DislikeReason;
import com.skala.ikgeoljune.domain.FeedbackType;
import jakarta.validation.constraints.NotNull;

/**
 * FEEDBACK-001 피드백 등록·변경.
 * LIKE 이면 dislikeReason 은 무시하고 null 로 저장한다.
 */
public record FeedbackUpsertRequest(
        @NotNull(message = "피드백 유형은 필수입니다.")
        FeedbackType feedbackType,

        DislikeReason dislikeReason
) {
}
