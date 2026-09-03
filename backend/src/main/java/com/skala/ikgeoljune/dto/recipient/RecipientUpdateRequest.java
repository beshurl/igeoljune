package com.skala.ikgeoljune.dto.recipient;

import jakarta.validation.constraints.Size;

/** RECIPIENT-004 추천 대상 수정 — 보낸 필드만 수정한다. */
public record RecipientUpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 50) String relationship,
        @Size(max = 30) String ageGroup,
        @Size(max = 20) String gender,
        @Size(max = 100) String job
) {
}
