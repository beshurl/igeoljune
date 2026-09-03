package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.recipient.RecipientCreateRequest;
import com.skala.ikgeoljune.dto.recipient.RecipientResponse;
import com.skala.ikgeoljune.dto.recipient.RecipientUpdateRequest;
import com.skala.ikgeoljune.repository.RecipientRepository;
import com.skala.ikgeoljune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** §4 추천 대상 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;
    private final OwnershipValidator ownershipValidator;
    private final CascadeDeleteService cascadeDeleteService;

    /** RECIPIENT-001 */
    @Transactional
    public RecipientResponse create(Long userId, RecipientCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Recipient recipient = Recipient.create(
                user,
                request.name(),
                request.relationship(),
                request.ageGroup(),
                request.gender(),
                request.job()
        );
        return RecipientResponse.from(recipientRepository.save(recipient));
    }

    /** RECIPIENT-002 — API.yml page/size 쿼리 파라미터 지원. totalCount 는 전체 건수다. */
    public ListResponse<RecipientResponse> findAll(Long userId, int page, int size) {
        Page<Recipient> result = recipientRepository.findByUserIdOrderByIdDesc(
                userId, PageRequest.of(page, size));
        List<RecipientResponse> items = result.getContent().stream()
                .map(RecipientResponse::from)
                .toList();
        return ListResponse.of(items, result.getTotalElements());
    }

    /** RECIPIENT-003 */
    public RecipientResponse findOne(Long recipientId, Long userId) {
        return RecipientResponse.from(ownershipValidator.getOwnedRecipient(recipientId, userId));
    }

    /** RECIPIENT-004 — 보낸 필드만 수정한다. */
    @Transactional
    public RecipientResponse update(Long recipientId, Long userId, RecipientUpdateRequest request) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        recipient.update(request.name(), request.relationship(), request.ageGroup(),
                request.gender(), request.job());
        return RecipientResponse.from(recipient);
    }

    /** RECIPIENT-005 — 하위 데이터를 함께 삭제한다. */
    @Transactional
    public void delete(Long recipientId, Long userId) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        cascadeDeleteService.deleteRecipients(List.of(recipient.getId()));
    }
}
