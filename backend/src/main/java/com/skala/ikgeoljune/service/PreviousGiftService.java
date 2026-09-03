package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.PreviousGift;
import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftCreateRequest;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftResponse;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftUpdateRequest;
import com.skala.ikgeoljune.repository.PreviousGiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** §6 과거 선물 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreviousGiftService {

    private final PreviousGiftRepository previousGiftRepository;
    private final OwnershipValidator ownershipValidator;

    /** PREVGIFT-001 */
    @Transactional
    public PreviousGiftResponse create(Long recipientId, Long userId, PreviousGiftCreateRequest request) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        PreviousGift gift = PreviousGift.create(
                recipient, request.giftName(), request.giftCategory(), request.giftedAt(), request.note());
        return PreviousGiftResponse.from(previousGiftRepository.save(gift));
    }

    /** PREVGIFT-002 */
    public ListResponse<PreviousGiftResponse> findAll(Long recipientId, Long userId) {
        ownershipValidator.getOwnedRecipient(recipientId, userId);
        List<PreviousGiftResponse> items =
                previousGiftRepository.findByRecipientIdOrderByGiftedAtDescIdDesc(recipientId).stream()
                        .map(PreviousGiftResponse::from)
                        .toList();
        return ListResponse.of(items);
    }

    /** PREVGIFT-003 — 보낸 필드만 수정한다. */
    @Transactional
    public PreviousGiftResponse update(Long previousGiftId, Long userId, PreviousGiftUpdateRequest request) {
        PreviousGift gift = ownershipValidator.getOwnedPreviousGift(previousGiftId, userId);
        gift.update(request.giftName(), request.giftCategory(), request.giftedAt(), request.note());
        return PreviousGiftResponse.from(gift);
    }

    /** PREVGIFT-004 */
    @Transactional
    public void delete(Long previousGiftId, Long userId) {
        PreviousGift gift = ownershipValidator.getOwnedPreviousGift(previousGiftId, userId);
        previousGiftRepository.delete(gift);
    }
}
