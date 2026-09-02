package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// SCR-HISTORY-001 · UC13·UC14 선물 확정 및 이력 관리
@Entity
@Table(name = "gift_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    // AI 추천을 통해 확정된 경우 연결 (직접 등록 시 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_candidate_id")
    private RecommendationCandidate recommendationCandidate;

    private String occasion;

    @Column(name = "gift_name", nullable = false)
    private String giftName;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
