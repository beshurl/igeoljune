package com.skala.ikgeoljune.service;

import com.skala.ikgeoljune.domain.Recipient;
import com.skala.ikgeoljune.domain.User;
import com.skala.ikgeoljune.dto.request.RecipientRequest;
import com.skala.ikgeoljune.dto.response.RecipientResponse;
import com.skala.ikgeoljune.exception.NotFoundException;
import com.skala.ikgeoljune.repository.RecipientRepository;
import com.skala.ikgeoljune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// SCR-RECIPIENT-001 · UC2 추천 대상 등록/조회/수정
@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RecipientResponse> findAll(Long userId) {
        return recipientRepository.findByUserId(userId).stream()
                .map(RecipientResponse::from)
                .toList();
    }

    @Transactional
    public RecipientResponse create(Long userId, RecipientRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));
        Recipient recipient = Recipient.builder()
                .user(user)
                .name(request.getName())
                .relationship(request.getRelationship())
                .age(request.getAge())
                .gender(request.getGender())
                .upcomingAnniversary(request.getUpcomingAnniversary())
                .excludeTags(request.getExcludeTags())
                .build();
        return RecipientResponse.from(recipientRepository.save(recipient));
    }

    @Transactional
    public RecipientResponse update(Long recipientId, RecipientRequest request) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("대상을 찾을 수 없습니다: " + recipientId));
        recipient.setName(request.getName());
        recipient.setRelationship(request.getRelationship());
        recipient.setAge(request.getAge());
        recipient.setGender(request.getGender());
        recipient.setUpcomingAnniversary(request.getUpcomingAnniversary());
        recipient.setExcludeTags(request.getExcludeTags());
        return RecipientResponse.from(recipient);
    }

    @Transactional
    public void delete(Long recipientId) {
        recipientRepository.deleteById(recipientId);
    }
}
