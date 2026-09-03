package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.*;
import com.skala.ikgeoljune.dto.preference.PreferenceBulkCreateRequest;
import com.skala.ikgeoljune.dto.preference.PreferenceCreateRequest;
import com.skala.ikgeoljune.dto.preference.PreferenceResponse;
import com.skala.ikgeoljune.dto.preference.PreferenceUpdateRequest;
import com.skala.ikgeoljune.repository.StructuredPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** §5 구조화 취향 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceService {

    private final StructuredPreferenceRepository preferenceRepository;
    private final OwnershipValidator ownershipValidator;

    /** PREF-001 — sourceType 은 서버가 DIRECT 로 설정한다. */
    @Transactional
    public PreferenceResponse create(Long recipientId, Long userId, PreferenceCreateRequest request) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        StructuredPreference preference = save(recipient, request.preferenceType(),
                request.preferenceValue(), SourceType.DIRECT);
        return PreferenceResponse.from(preference);
    }

    /**
     * PREF-002 추출 취향 일괄 저장.
     * §13 (recipient_id, preference_type, preference_value) 중복 항목은 건너뛰고 신규만 저장한다.
     */
    @Transactional
    public ListResponse<PreferenceResponse> createBulk(Long recipientId, Long userId,
                                                       PreferenceBulkCreateRequest request) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);

        List<PreferenceResponse> saved = new ArrayList<>();
        for (PreferenceBulkCreateRequest.Item item : request.items()) {
            if (isDuplicated(recipientId, item.preferenceType(), item.preferenceValue())) {
                continue;
            }
            StructuredPreference preference = StructuredPreference.create(
                    recipient, item.preferenceType(), item.preferenceValue(), request.sourceType());
            saved.add(PreferenceResponse.from(preferenceRepository.save(preference)));
        }
        return ListResponse.of(saved);
    }

    /** PREF-003 — preferenceType / sourceType 선택 필터 */
    public ListResponse<PreferenceResponse> findAll(Long recipientId, Long userId,
                                                    PreferenceType preferenceType, SourceType sourceType) {
        ownershipValidator.getOwnedRecipient(recipientId, userId);
        List<PreferenceResponse> items = preferenceRepository.search(recipientId, preferenceType, sourceType).stream()
                .map(PreferenceResponse::from)
                .toList();
        return ListResponse.of(items);
    }

    /** PREF-004 — 보낸 필드만 수정한다. */
    @Transactional
    public PreferenceResponse update(Long preferenceId, Long userId, PreferenceUpdateRequest request) {
        StructuredPreference preference = ownershipValidator.getOwnedPreference(preferenceId, userId);

        PreferenceType newType = request.preferenceType() != null
                ? request.preferenceType() : preference.getPreferenceType();
        String newValue = request.preferenceValue() != null
                ? request.preferenceValue() : preference.getPreferenceValue();

        boolean changed = newType != preference.getPreferenceType()
                || !newValue.equals(preference.getPreferenceValue());
        if (changed && isDuplicated(preference.getRecipient().getId(), newType, newValue)) {
            throw new ApiException(ErrorCode.PREFERENCE_DUPLICATED);
        }

        preference.update(request.preferenceType(), request.preferenceValue());
        return PreferenceResponse.from(preference);
    }

    /** PREF-005 */
    @Transactional
    public void delete(Long preferenceId, Long userId) {
        StructuredPreference preference = ownershipValidator.getOwnedPreference(preferenceId, userId);
        preferenceRepository.delete(preference);
    }

    private StructuredPreference save(Recipient recipient, PreferenceType type, String value, SourceType sourceType) {
        if (isDuplicated(recipient.getId(), type, value)) {
            throw new ApiException(ErrorCode.PREFERENCE_DUPLICATED);
        }
        return preferenceRepository.save(StructuredPreference.create(recipient, type, value, sourceType));
    }

    private boolean isDuplicated(Long recipientId, PreferenceType type, String value) {
        return preferenceRepository.existsByRecipientIdAndPreferenceTypeAndPreferenceValue(recipientId, type, value);
    }
}
