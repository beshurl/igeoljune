package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.auth.*;
import com.skala.ikgeoljune.dto.user.UserResponse;
import com.skala.ikgeoljune.repository.UserRepository;
import com.skala.ikgeoljune.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** §3 회원·인증 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** AUTH-001 회원가입 */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        // DB.dbml users.email: 로그인 식별자. 소문자 정규화 후 저장
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(ErrorCode.RESOURCE_CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.name()
        );
        return UserResponse.from(userRepository.save(user));
    }

    /** AUTH-002 로그인 */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        return LoginResponse.of(accessToken, jwtTokenProvider.getExpiresInSeconds(), user);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
