package com.skala.ikgeoljune.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * created_at / updated_at 공통 컬럼 (§1.1 일시는 ISO 8601).
 *
 * <p>updated_at 은 {@code @PreUpdate}(flush 시점) 대신 도메인 수정 메서드가 직접 {@link #touch()} 로 갱신한다.
 * 수정 API 는 flush 전에 응답 DTO 를 만들기 때문에, flush 시점에 갱신하면
 * 응답의 updatedAt 과 실제 저장 값이 어긋난다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 상태를 변경하는 도메인 메서드가 호출해 수정 시각을 즉시 반영한다. */
    protected void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}
