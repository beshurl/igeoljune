package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.GiftHistory;
import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.domain.RecommendationCandidate;
import com.skala.ikgeoljune.dto.request.GiftConfirmRequest;
import com.skala.ikgeoljune.exception.NotFoundException;
import com.skala.ikgeoljune.repository.GiftHistoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// SCR-HISTORY-001 · UC13 선물 확정 및 이력 저장, UC14 이력 조회
@Service
@RequiredArgsConstructor
public class GiftHistoryService {

    private final GiftHistoryRepository giftHistoryRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<GiftHistory> findByRecipient(Long recipientId) {
        return giftHistoryRepository.findByRecipientId(recipientId);
    }

    // UC13: SCR-AI-001/002에서 확인한 추천 후보를 최종 선물로 확정하고 이력에 저장
    // recipient는 candidate -> recommendation -> giftCondition -> recipient 경로로 역추적한다.
    @Transactional
    public Long confirm(Long recommendationId, GiftConfirmRequest request) {
        RecommendationCandidate candidate = entityManager.find(RecommendationCandidate.class, request.getCandidateId());
        if (candidate == null || !candidate.getRecommendation().getId().equals(recommendationId)) {
            throw new NotFoundException("추천 후보를 찾을 수 없습니다: " + request.getCandidateId());
        }

        Recipient recipient = candidate.getRecommendation().getGiftCondition().getRecipient();

        GiftHistory history = GiftHistory.builder()
                .recipient(recipient)
                .recommendationCandidate(candidate)
                .occasion(request.getOccasion())
                .giftName(candidate.getName())
                .confirmedDate(request.getConfirmedDate())
                .build();

        return giftHistoryRepository.save(history).getId();
    }
}
