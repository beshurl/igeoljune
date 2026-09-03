package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftCreateRequest;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftResponse;
import com.skala.ikgeoljune.dto.previousgift.PreviousGiftUpdateRequest;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.PreviousGiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §6 과거 선물 API */
@Tag(name = "PreviousGift", description = "과거 선물")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PreviousGiftController {

    private final PreviousGiftService previousGiftService;

    @Operation(summary = "PREVGIFT-001 과거 선물 등록")
    @PostMapping("/recipients/{recipientId}/previous-gifts")
    @ResponseStatus(HttpStatus.CREATED)
    public PreviousGiftResponse create(@CurrentUser AuthUser authUser,
                                       @PathVariable Long recipientId,
                                       @Valid @RequestBody PreviousGiftCreateRequest request) {
        return previousGiftService.create(recipientId, authUser.userId(), request);
    }

    @Operation(summary = "PREVGIFT-002 과거 선물 목록 조회")
    @GetMapping("/recipients/{recipientId}/previous-gifts")
    public ListResponse<PreviousGiftResponse> findAll(@CurrentUser AuthUser authUser,
                                                      @PathVariable Long recipientId) {
        return previousGiftService.findAll(recipientId, authUser.userId());
    }

    @Operation(summary = "PREVGIFT-003 과거 선물 수정")
    @PatchMapping("/previous-gifts/{previousGiftId}")
    public PreviousGiftResponse update(@CurrentUser AuthUser authUser,
                                       @PathVariable Long previousGiftId,
                                       @Valid @RequestBody PreviousGiftUpdateRequest request) {
        return previousGiftService.update(previousGiftId, authUser.userId(), request);
    }

    @Operation(summary = "PREVGIFT-004 과거 선물 삭제")
    @DeleteMapping("/previous-gifts/{previousGiftId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthUser authUser, @PathVariable Long previousGiftId) {
        previousGiftService.delete(previousGiftId, authUser.userId());
    }
}
