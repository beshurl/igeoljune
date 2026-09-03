package com.skala.ikgeoljune.dto.auth;

import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.user.UserResponse;

/** API.yml LoginResponse — user 는 User 스키마 전체를 담는다. */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static LoginResponse of(String accessToken, long expiresIn, User user) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, UserResponse.from(user));
    }
}
