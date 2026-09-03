package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.auth.*;
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
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_DUPLICATED);
        }
        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name()
        );
        return SignupResponse.from(userRepository.save(user));
    }

    /** AUTH-002 로그인 */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        return LoginResponse.of(accessToken, jwtTokenProvider.getExpiresInSeconds(), user);
    }
}
