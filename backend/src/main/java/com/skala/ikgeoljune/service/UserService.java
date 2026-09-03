package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.user.UpdateUserNameRequest;
import com.skala.ikgeoljune.dto.user.UserResponse;
import com.skala.ikgeoljune.repository.RecipientRepository;
import com.skala.ikgeoljune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** §3 USER-001 ~ USER-003 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final CascadeDeleteService cascadeDeleteService;

    /** USER-001 내 정보 조회 */
    public UserResponse getMe(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    /** USER-002 내 이름 수정 */
    @Transactional
    public UserResponse updateName(Long userId, UpdateUserNameRequest request) {
        User user = getUser(userId);
        user.changeName(request.name());
        return UserResponse.from(user);
    }

    /** USER-003 회원 탈퇴 — 사용자에 속한 추천 대상과 하위 데이터를 함께 삭제한다. */
    @Transactional
    public void withdraw(Long userId) {
        User user = getUser(userId);
        List<Long> recipientIds = recipientRepository.findIdsByUserId(userId);
        cascadeDeleteService.deleteRecipients(recipientIds);
        userRepository.delete(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
