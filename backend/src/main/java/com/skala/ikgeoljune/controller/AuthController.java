package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.auth.LoginRequest;
import com.skala.ikgeoljune.dto.auth.LoginResponse;
import com.skala.ikgeoljune.dto.auth.SignupRequest;
import com.skala.ikgeoljune.dto.auth.SignupResponse;
import com.skala.ikgeoljune.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §3 회원·인증 API (인증 불필요) */
@Tag(name = "Auth", description = "회원가입·로그인")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "AUTH-001 회원가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @Operation(summary = "AUTH-002 로그인")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
