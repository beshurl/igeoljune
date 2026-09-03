package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구조화 취향.
 * §13: (recipient_id, preference_type, preference_value) 유일 제약으로 중복 저장을 막는다.
 */
@Entity
@Getter
@Table(
        name = "structured_preference",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_preference_recipient_type_value",
                columnNames = {"recipient_id", "preference_type", "preference_value"}
        ),
        indexes = @Index(name = "idx_preference_recipient_id", columnList = "recipient_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StructuredPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_type", nullable = false, length = 50)
    private PreferenceType preferenceType;

    @Column(name = "preference_value", nullable = false, length = 255)
    private String preferenceValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    private StructuredPreference(Recipient recipient, PreferenceType preferenceType,
                                 String preferenceValue, SourceType sourceType) {
        this.recipient = recipient;
        this.preferenceType = preferenceType;
        this.preferenceValue = preferenceValue;
        this.sourceType = sourceType;
    }

    public static StructuredPreference create(Recipient recipient, PreferenceType preferenceType,
                                              String preferenceValue, SourceType sourceType) {
        return new StructuredPreference(recipient, preferenceType, preferenceValue, sourceType);
    }

    /** PREF-004: 보낸 필드만 수정한다. sourceType 은 변경하지 않는다. */
    public void update(PreferenceType preferenceType, String preferenceValue) {
        if (preferenceType != null) this.preferenceType = preferenceType;
        if (preferenceValue != null) this.preferenceValue = preferenceValue;
    }
}
