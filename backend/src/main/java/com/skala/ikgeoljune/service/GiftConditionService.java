package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.common.ApiException;
import com.skala.ikgeoljune.common.ErrorCode;
import com.skala.ikgeoljune.domain.GiftCondition;
import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.dto.condition.GiftConditionCreateRequest;
import com.skala.ikgeoljune.dto.condition.GiftConditionResponse;
import com.skala.ikgeoljune.dto.condition.GiftConditionUpdateRequest;
import com.skala.ikgeoljune.repository.GiftConditionRepository;
import com.skala.ikgeoljune.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** §7 추천 조건 API */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiftConditionService {

    private final GiftConditionRepository giftConditionRepository;
    private final RecommendationRepository recommendationRepository;
    private final OwnershipValidator ownershipValidator;

    /** CONDITION-001 */
    @Transactional
    public GiftConditionResponse create(Long recipientId, Long userId, GiftConditionCreateRequest request) {
        Recipient recipient = ownershipValidator.getOwnedRecipient(recipientId, userId);
        validateBudget(request.budgetMin(), request.budgetMax());

        GiftCondition condition = GiftCondition.create(
                recipient,
                request.budgetMin(),
                request.budgetMax(),
                request.occasionType(),
                request.occasionDate(),
                request.preferenceNote(),
                request.avoidGiftNote()
        );
        return GiftConditionResponse.from(giftConditionRepository.save(condition));
    }

    /** CONDITION-002 */
    public GiftConditionResponse findOne(Long conditionId, Long userId) {
        return GiftConditionResponse.from(ownershipValidator.getOwnedCondition(conditionId, userId));
    }

    /** CONDITION-003 — 보낸 필드만 수정한다. */
    @Transactional
    public GiftConditionResponse update(Long conditionId, Long userId, GiftConditionUpdateRequest request) {
        GiftCondition condition = ownershipValidator.getOwnedCondition(conditionId, userId);

        int newMin = request.budgetMin() != null ? request.budgetMin() : condition.getBudgetMin();
        int newMax = request.budgetMax() != null ? request.budgetMax() : condition.getBudgetMax();
        validateBudget(newMin, newMax);

        condition.update(request.budgetMin(), request.budgetMax(), request.occasionType(),
                request.occasionDate(), request.preferenceNote(), request.avoidGiftNote());
        return GiftConditionResponse.from(condition);
    }

    /**
     * CONDITION-004.
     * MVP 정책: 연결된 추천 결과가 있으면 오조작 방지를 위해 409 Conflict 를 반환한다.
     */
    @Transactional
    public void delete(Long conditionId, Long userId) {
        GiftCondition condition = ownershipValidator.getOwnedCondition(conditionId, userId);
        if (recommendationRepository.existsByConditionId(conditionId)) {
            throw new ApiException(ErrorCode.GIFT_CONDITION_HAS_RECOMMENDATIONS);
        }
        giftConditionRepository.delete(condition);
    }

    /** budgetMin·budgetMax 는 0 이상이며 budgetMin 은 budgetMax 보다 클 수 없다. */
    private void validateBudget(int budgetMin, int budgetMax) {
        if (budgetMin > budgetMax) {
            throw new ApiException(ErrorCode.INVALID_BUDGET_RANGE);
        }
    }
}
