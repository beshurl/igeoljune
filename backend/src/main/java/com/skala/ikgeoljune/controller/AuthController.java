package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.security.JwtTokenProvider;
import com.skala.ikgeoljune.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// SCR-AUTH-001 · UC1
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/google-login")
    public Map<String, Object> googleLogin(@RequestBody Map<String, String> body) {
        User user = authService.loginWithGoogle(body.get("idToken"));
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        return Map.of(
                "accessToken", accessToken,
                "user", Map.of("id", user.getId(), "name", user.getName(), "email", user.getEmail())
        );
    }
}
