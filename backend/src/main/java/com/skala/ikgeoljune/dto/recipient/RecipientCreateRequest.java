package com.skala.ikgeoljune.dto.recipient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RECIPIENT-001 추천 대상 등록 */
public record RecipientCreateRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100)
        String name,

        @NotBlank(message = "관계는 필수입니다.")
        @Size(max = 50)
        String relationship,

        @NotBlank(message = "연령대는 필수입니다.")
        @Size(max = 30)
        String ageGroup,

        @NotBlank(message = "성별은 필수입니다.")
        @Size(max = 20)
        String gender,

        @Size(max = 100)
        String job
) {
}
