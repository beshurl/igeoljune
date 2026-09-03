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
                name = "uk_candidate_recommendation_rank",
                columnNames = {"recommendation_id", "recommendation_rank"}
        )
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

    @Column(name = "recommendation_rank", nullable = false)
    private Integer recommendationRank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    private RecommendationCandidate(Recommendation recommendation, String giftName, String giftCategory,
                                    Integer estimatedPriceMin, Integer estimatedPriceMax,
                                    String recommendationReason, String consideredInfo,
                                    String cautionNote, Integer recommendationRank) {
        this.recommendation = recommendation;
        this.giftName = giftName;
        this.giftCategory = giftCategory;
        this.estimatedPriceMin = estimatedPriceMin;
        this.estimatedPriceMax = estimatedPriceMax;
        this.recommendationReason = recommendationReason;
        this.consideredInfo = consideredInfo;
        this.cautionNote = cautionNote;
        this.recommendationRank = recommendationRank;
    }

    public static RecommendationCandidate create(Recommendation recommendation, String giftName, String giftCategory,
                                                 Integer estimatedPriceMin, Integer estimatedPriceMax,
                                                 String recommendationReason, String consideredInfo,
                                                 String cautionNote, Integer recommendationRank) {
        return new RecommendationCandidate(recommendation, giftName, giftCategory, estimatedPriceMin,
                estimatedPriceMax, recommendationReason, consideredInfo, cautionNote, recommendationRank);
    }
}
