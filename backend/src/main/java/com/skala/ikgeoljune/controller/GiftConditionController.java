package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.request.GiftConditionRequest;
import com.skala.ikgeoljune.service.GiftRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// SCR-GIFT-001 · UC7 (대표 흐름 시작점)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GiftConditionController {

    private final GiftRecommendationService giftRecommendationService;

    @PostMapping("/recipients/{recipientId}/gift-conditions")
    public Map<String, Long> create(@PathVariable Long recipientId,
                                     @Valid @RequestBody GiftConditionRequest request) {
        Long id = giftRecommendationService.createCondition(recipientId, request);
        return Map.of("id", id);
    }
}
