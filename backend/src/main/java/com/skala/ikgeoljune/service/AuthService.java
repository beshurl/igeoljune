package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// SCR-AUTH-001 · UC1 Google 로그인
// TODO: 실제 Google OAuth ID Token 검증 로직 연동 (google-api-client 등)
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public User loginWithGoogle(String idToken) {
        // 임시: idToken을 googleSub로 그대로 사용 (실제로는 토큰 검증 후 sub claim 추출)
        return userRepository.findByGoogleSub(idToken)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .googleSub(idToken)
                                .email("unknown@example.com")
                                .name("신규 사용자")
                                .build()
                ));
    }
}
