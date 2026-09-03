package com.skala.ikgeoljune.dto.recipient;

import com.skala.ikgeoljune.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.Size;

/**
 * RECIPIENT-004 추천 대상 수정 — 보낸 필드만 수정한다.
 * 필드를 보냈다면 생성 요청과 동일하게 공백일 수 없다.
 */
public record RecipientUpdateRequest(
        @NotBlankIfPresent(message = "이름은 공백일 수 없습니다.")
        @Size(max = 100) String name,

        @NotBlankIfPresent(message = "관계는 공백일 수 없습니다.")
        @Size(max = 50) String relationship,

        @NotBlankIfPresent(message = "연령대는 공백일 수 없습니다.")
        @Size(max = 30) String ageGroup,

        @NotBlankIfPresent(message = "성별은 공백일 수 없습니다.")
        @Size(max = 20) String gender,

        @Size(max = 100) String job
) {
}
