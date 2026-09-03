package com.skala.ikgeoljune.dto.auth;

import com.skala.ikgeoljune.domain.User;

import java.time.OffsetDateTime;

/** AUTH-001 회원가입 응답 (201 Created) */
public record SignupResponse(
        Long userId,
        String email,
        String name,
        OffsetDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
    }
}
