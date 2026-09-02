package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.*;

// SCR-AI-001/002 · UC9~UC11 추천 후보 (가격·이유) + 피드백
@Entity
@Table(name = "recommendation_candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "image_url")
    private String imageUrl;

    // UC10 좋아요/싫어요 피드백 (null = 아직 피드백 없음)
    private Boolean liked;

    @Column(name = "dislike_reason")
    private String dislikeReason;
}
