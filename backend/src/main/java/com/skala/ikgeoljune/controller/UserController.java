package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.user.UpdateUserNameRequest;
import com.skala.ikgeoljune.dto.user.UserResponse;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §3 USER-001 ~ USER-003 */
@Tag(name = "User", description = "내 정보")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "USER-001 내 정보 조회")
    @GetMapping("/me")
    public UserResponse getMe(@CurrentUser AuthUser authUser) {
        return userService.getMe(authUser.userId());
    }

    @Operation(summary = "USER-002 내 이름 수정")
    @PatchMapping("/me")
    public UserResponse updateName(@CurrentUser AuthUser authUser,
                                   @Valid @RequestBody UpdateUserNameRequest request) {
        return userService.updateName(authUser.userId(), request);
    }

    @Operation(summary = "USER-003 회원 탈퇴")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@CurrentUser AuthUser authUser) {
        userService.withdraw(authUser.userId());
    }
}
