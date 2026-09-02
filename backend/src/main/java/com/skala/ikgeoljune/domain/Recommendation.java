package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// SCR-AI-001 ★핵심 · UC8(AI 확장 지점)~UC9 AI 추천 결과
@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_condition_id", nullable = false)
    private GiftCondition giftCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    // UC12 재추천 시 이전 추천을 가리켜 이력 추적
    @Column(name = "previous_recommendation_id")
    private Long previousRecommendationId;

    @Column(name = "failure_reason")
    private String failureReason;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecommendationCandidate> candidates = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
