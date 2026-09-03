package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** 추천 조건 (§7). ERD 상 updated_at 이 없어 created_at 만 관리한다. */
@Entity
@Getter
@Table(name = "gift_conditions", indexes = @Index(name = "idx_gift_conditions_recipient_id", columnList = "recipient_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiftCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Column(name = "budget_min", nullable = false)
    private Integer budgetMin;

    @Column(name = "budget_max", nullable = false)
    private Integer budgetMax;

    /** 예: BIRTHDAY, ANNIVERSARY — 명세서에 값 목록이 없어 문자열로 유지한다. */
    @Column(name = "occasion_type", nullable = false, length = 100)
    private String occasionType;

    @Column(name = "occasion_date")
    private LocalDate occasionDate;

    @Column(name = "preference_note", columnDefinition = "text")
    private String preferenceNote;

    @Column(name = "avoid_gift_note", columnDefinition = "text")
    private String avoidGiftNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    private GiftCondition(Recipient recipient, Integer budgetMin, Integer budgetMax, String occasionType,
                          LocalDate occasionDate, String preferenceNote, String avoidGiftNote) {
        this.recipient = recipient;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.occasionType = occasionType;
        this.occasionDate = occasionDate;
        this.preferenceNote = preferenceNote;
        this.avoidGiftNote = avoidGiftNote;
    }

    public static GiftCondition create(Recipient recipient, Integer budgetMin, Integer budgetMax,
                                       String occasionType, LocalDate occasionDate,
                                       String preferenceNote, String avoidGiftNote) {
        return new GiftCondition(recipient, budgetMin, budgetMax, occasionType, occasionDate,
                preferenceNote, avoidGiftNote);
    }

    /** CONDITION-003: 보낸 필드만 수정한다. */
    public void update(Integer budgetMin, Integer budgetMax, String occasionType,
                       LocalDate occasionDate, String preferenceNote, String avoidGiftNote) {
        if (budgetMin != null) this.budgetMin = budgetMin;
        if (budgetMax != null) this.budgetMax = budgetMax;
        if (occasionType != null) this.occasionType = occasionType;
        if (occasionDate != null) this.occasionDate = occasionDate;
        if (preferenceNote != null) this.preferenceNote = preferenceNote;
        if (avoidGiftNote != null) this.avoidGiftNote = avoidGiftNote;
    }
}
