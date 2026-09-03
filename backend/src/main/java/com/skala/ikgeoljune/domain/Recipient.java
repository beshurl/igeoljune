package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "recipient", indexes = @Index(name = "idx_recipient_user_id", columnList = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 예: FRIEND, FAMILY, COLLEAGUE — 명세서에 값 목록이 없어 문자열로 유지한다. */
    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    /** 예: LATE_20S */
    @Column(name = "age_group", nullable = false, length = 30)
    private String ageGroup;

    /** 예: MALE, FEMALE */
    @Column(name = "gender", nullable = false, length = 20)
    private String gender;

    @Column(name = "job", length = 100)
    private String job;

    private Recipient(User user, String name, String relationship, String ageGroup, String gender, String job) {
        this.user = user;
        this.name = name;
        this.relationship = relationship;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.job = job;
    }

    public static Recipient create(User user, String name, String relationship,
                                   String ageGroup, String gender, String job) {
        return new Recipient(user, name, relationship, ageGroup, gender, job);
    }

    /** RECIPIENT-004: 보낸 필드만 수정한다. */
    public void update(String name, String relationship, String ageGroup, String gender, String job) {
        if (name != null) this.name = name;
        if (relationship != null) this.relationship = relationship;
        if (ageGroup != null) this.ageGroup = ageGroup;
        if (gender != null) this.gender = gender;
        if (job != null) this.job = job;
        touch();
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }
}
