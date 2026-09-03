package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.dto.recipient.RecipientCreateRequest;
import com.skala.ikgeoljune.dto.recipient.RecipientResponse;
import com.skala.ikgeoljune.dto.recipient.RecipientUpdateRequest;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** §4 추천 대상 API */
@Tag(name = "Recipient", description = "추천 대상")
@Validated
@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    @Operation(summary = "RECIPIENT-001 추천 대상 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipientResponse create(@CurrentUser AuthUser authUser,
                                    @Valid @RequestBody RecipientCreateRequest request) {
        return recipientService.create(authUser.userId(), request);
    }

    @Operation(summary = "RECIPIENT-002 추천 대상 목록 조회")
    @GetMapping
    public ListResponse<RecipientResponse> findAll(
            @CurrentUser AuthUser authUser,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page 는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size 는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size 는 100 이하여야 합니다.") int size) {
        return recipientService.findAll(authUser.userId(), page, size);
    }

    @Operation(summary = "RECIPIENT-003 추천 대상 상세 조회")
    @GetMapping("/{recipientId}")
    public RecipientResponse findOne(@CurrentUser AuthUser authUser, @PathVariable Long recipientId) {
        return recipientService.findOne(recipientId, authUser.userId());
    }

    @Operation(summary = "RECIPIENT-004 추천 대상 수정")
    @PatchMapping("/{recipientId}")
    public RecipientResponse update(@CurrentUser AuthUser authUser,
                                    @PathVariable Long recipientId,
                                    @Valid @RequestBody RecipientUpdateRequest request) {
        return recipientService.update(recipientId, authUser.userId(), request);
    }

    @Operation(summary = "RECIPIENT-005 추천 대상 삭제")
    @DeleteMapping("/{recipientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthUser authUser, @PathVariable Long recipientId) {
        recipientService.delete(recipientId, authUser.userId());
    }
}
