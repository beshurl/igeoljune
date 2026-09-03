package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 추천 후보 (§8).
 * §13: (recommendation_id, recommendation_rank) 유일 제약을 준수한다.
 */
@Entity
@Getter
@Table(
        name = "recommendation_candidates",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_candidate_recommend_rank",
                columnNames = {"recommendation_id", "recommend_rank"}
        ),
        indexes = @Index(name = "idx_candidates_recommendation_id", columnList = "recommendation_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(name = "gift_name", nullable = false, length = 255)
    private String giftName;

    @Column(name = "gift_category", length = 100)
    private String giftCategory;

    @Column(name = "estimated_price_min")
    private Integer estimatedPriceMin;

    @Column(name = "estimated_price_max")
    private Integer estimatedPriceMax;

    @Column(name = "recommendation_reason", nullable = false, columnDefinition = "text")
    private String recommendationReason;

    @Column(name = "considered_info", columnDefinition = "text")
    private String consideredInfo;

    @Column(name = "caution_note", columnDefinition = "text")
    private String cautionNote;

    @Column(name = "recommend_rank", nullable = false)
    private Integer recommendRank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** 최종 선물로 선택한 시각. 선택하지 않았으면 null */
    @Column(name = "selected_at")
    private OffsetDateTime selectedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    private RecommendationCandidate(Recommendation recommendation, String giftName, String giftCategory,
                                    Integer estimatedPriceMin, Integer estimatedPriceMax,
                                    String recommendationReason, String consideredInfo,
                                    String cautionNote, Integer recommendRank) {
        this.recommendation = recommendation;
        this.giftName = giftName;
        this.giftCategory = giftCategory;
        this.estimatedPriceMin = estimatedPriceMin;
        this.estimatedPriceMax = estimatedPriceMax;
        this.recommendationReason = recommendationReason;
        this.consideredInfo = consideredInfo;
        this.cautionNote = cautionNote;
        this.recommendRank = recommendRank;
    }

    public static RecommendationCandidate create(Recommendation recommendation, String giftName, String giftCategory,
                                                 Integer estimatedPriceMin, Integer estimatedPriceMax,
                                                 String recommendationReason, String consideredInfo,
                                                 String cautionNote, Integer recommendRank) {
        return new RecommendationCandidate(recommendation, giftName, giftCategory, estimatedPriceMin,
                estimatedPriceMax, recommendationReason, consideredInfo, cautionNote, recommendRank);
    }

    /** 최종 선물로 선택한다. */
    public void select() {
        this.selectedAt = OffsetDateTime.now();
    }

    /** 선택을 취소한다. */
    public void deselect() {
        this.selectedAt = null;
    }

    public boolean isSelected() {
        return this.selectedAt != null;
    }
}
