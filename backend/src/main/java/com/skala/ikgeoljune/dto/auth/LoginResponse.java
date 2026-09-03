package com.skala.ikgeoljune.dto.auth;

import com.skala.ikgeoljune.domain.User;

/** AUTH-002 로그인 응답 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        LoginUser user
) {
    public record LoginUser(Long userId, String email, String name) {
        public static LoginUser from(User user) {
            return new LoginUser(user.getId(), user.getEmail(), user.getName());
        }
    }

    public static LoginResponse of(String accessToken, long expiresIn, User user) {
        return new LoginResponse(accessToken, "Bearer", expiresIn, LoginUser.from(user));
    }
}
