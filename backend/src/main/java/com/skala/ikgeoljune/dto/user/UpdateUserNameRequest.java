package com.skala.ikgeoljune.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** USER-002 내 이름 수정 */
public record UpdateUserNameRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100)
        String name
) {
}
