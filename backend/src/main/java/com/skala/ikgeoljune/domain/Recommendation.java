package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 추천 실행 기록 (§8).
 * 재추천은 새 레코드를 만들고 previous_recommendation_id 로 이전 추천을 연결한다.
 */
@Entity
@Getter
@Table(name = "recommendations", indexes = @Index(name = "idx_recommendations_condition_id", columnList = "condition_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condition_id", nullable = false)
    private GiftCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_recommendation_id")
    private Recommendation previousRecommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecommendationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("recommendationRank asc")
    private List<RecommendationCandidate> candidates = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    private Recommendation(GiftCondition condition, Recommendation previousRecommendation) {
        this.condition = condition;
        this.previousRecommendation = previousRecommendation;
        this.status = RecommendationStatus.PROCESSING;
    }

    /** RECOMMEND-001 최초 추천 */
    public static Recommendation start(GiftCondition condition) {
        return new Recommendation(condition, null);
    }

    /** RECOMMEND-004 피드백 반영 재추천 */
    public static Recommendation reRecommend(GiftCondition condition, Recommendation previous) {
        return new Recommendation(condition, previous);
    }

    public void addCandidate(RecommendationCandidate candidate) {
        this.candidates.add(candidate);
    }

    public void markSuccess() {
        this.status = RecommendationStatus.SUCCESS;
    }

    public void markFailed() {
        this.status = RecommendationStatus.FAILED;
    }

    public Long getPreviousRecommendationId() {
        return previousRecommendation == null ? null : previousRecommendation.getId();
    }
}
