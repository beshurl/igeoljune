package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// SCR-RECIPIENT-001 · UC2 추천 대상(비공개 프로필)
@Entity
@Table(name = "recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String relationship;

    private Integer age;

    private String gender;

    @Column(name = "upcoming_anniversary")
    private LocalDate upcomingAnniversary;

    // UC3 이전 선물·제외 유형 (쉼표 구분 저장, 필요 시 별도 테이블로 정규화)
    @Column(name = "exclude_tags")
    private String excludeTags;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
