package com.skala.ikgeoljune.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "previous_gifts", indexes = @Index(name = "idx_previous_gifts_recipient_id", columnList = "recipient_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreviousGift extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "previous_gift_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Column(name = "gift_name", nullable = false, length = 255)
    private String giftName;

    @Column(name = "gift_category", length = 100)
    private String giftCategory;

    @Column(name = "gifted_at")
    private LocalDate giftedAt;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    private PreviousGift(Recipient recipient, String giftName, String giftCategory, LocalDate giftedAt, String note) {
        this.recipient = recipient;
        this.giftName = giftName;
        this.giftCategory = giftCategory;
        this.giftedAt = giftedAt;
        this.note = note;
    }

    public static PreviousGift create(Recipient recipient, String giftName, String giftCategory,
                                      LocalDate giftedAt, String note) {
        return new PreviousGift(recipient, giftName, giftCategory, giftedAt, note);
    }

    /** PREVGIFT-003: 보낸 필드만 수정한다. */
    public void update(String giftName, String giftCategory, LocalDate giftedAt, String note) {
        if (giftName != null) this.giftName = giftName;
        if (giftCategory != null) this.giftCategory = giftCategory;
        if (giftedAt != null) this.giftedAt = giftedAt;
        if (note != null) this.note = note;
    }
}
