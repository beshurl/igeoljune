package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// SCR-GIFT-001 · UC7 (대표 흐름) 선물 조건 입력 — 예산 최우선 원칙
@Entity
@Table(name = "gift_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    // 필수 조건: 예산 최우선
    @Column(nullable = false)
    private Integer budget;

    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate;

    // 취향/제외 조건 (쉼표 구분 저장, 필요 시 정규화)
    @Column(name = "preference_tags")
    private String preferenceTags;

    @Column(name = "exclude_tags")
    private String excludeTags;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
