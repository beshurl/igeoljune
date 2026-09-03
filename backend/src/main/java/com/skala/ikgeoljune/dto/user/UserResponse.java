package com.skala.ikgeoljune.dto.user;

import com.skala.ikgeoljune.domain.User;

import java.time.OffsetDateTime;

/** USER-001 / USER-002 응답 */
public record UserResponse(
        Long userId,
        String email,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
