package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 후보별 피드백 (§9).
 * candidate_id 유일 제약으로 후보당 0개 또는 1개만 유지한다.
 */
@Entity
@Getter
@Table(
        name = "feedback",
        uniqueConstraints = @UniqueConstraint(name = "uk_feedback_candidate_id", columnNames = "candidate_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private RecommendationCandidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20)
    private FeedbackType feedbackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "dislike_reason", length = 255)
    private DislikeReason dislikeReason;

    private Feedback(RecommendationCandidate candidate, FeedbackType feedbackType, DislikeReason dislikeReason) {
        this.candidate = candidate;
        apply(feedbackType, dislikeReason);
    }

    public static Feedback create(RecommendationCandidate candidate, FeedbackType feedbackType,
                                  DislikeReason dislikeReason) {
        return new Feedback(candidate, feedbackType, dislikeReason);
    }

    /**
     * FEEDBACK-001 upsert.
     * LIKE 이면 dislikeReason 을 null 로 저장하고, DISLIKE 이면 선택적으로 저장한다.
     */
    public void apply(FeedbackType feedbackType, DislikeReason dislikeReason) {
        this.feedbackType = feedbackType;
        this.dislikeReason = (feedbackType == FeedbackType.DISLIKE) ? dislikeReason : null;
        touch();
    }
}
