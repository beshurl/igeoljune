package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.condition.GiftConditionCreateRequest;
import com.skala.ikgeoljune.dto.condition.GiftConditionResponse;
import com.skala.ikgeoljune.dto.condition.GiftConditionUpdateRequest;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.GiftConditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §7 추천 조건 API */
@Tag(name = "GiftCondition", description = "추천 조건")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GiftConditionController {

    private final GiftConditionService giftConditionService;

    @Operation(summary = "CONDITION-001 추천 조건 생성")
    @PostMapping("/recipients/{recipientId}/gift-conditions")
    @ResponseStatus(HttpStatus.CREATED)
    public GiftConditionResponse create(@CurrentUser AuthUser authUser,
                                        @PathVariable Long recipientId,
                                        @Valid @RequestBody GiftConditionCreateRequest request) {
        return giftConditionService.create(recipientId, authUser.userId(), request);
    }

    @Operation(summary = "CONDITION-002 추천 조건 조회")
    @GetMapping("/gift-conditions/{conditionId}")
    public GiftConditionResponse findOne(@CurrentUser AuthUser authUser, @PathVariable Long conditionId) {
        return giftConditionService.findOne(conditionId, authUser.userId());
    }

    @Operation(summary = "CONDITION-003 추천 조건 수정")
    @PatchMapping("/gift-conditions/{conditionId}")
    public GiftConditionResponse update(@CurrentUser AuthUser authUser,
                                        @PathVariable Long conditionId,
                                        @Valid @RequestBody GiftConditionUpdateRequest request) {
        return giftConditionService.update(conditionId, authUser.userId(), request);
    }

    @Operation(summary = "CONDITION-004 추천 조건 삭제 (추천 결과가 있으면 409)")
    @DeleteMapping("/gift-conditions/{conditionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthUser authUser, @PathVariable Long conditionId) {
        giftConditionService.delete(conditionId, authUser.userId());
    }
}
