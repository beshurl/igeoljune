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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §4 추천 대상 API */
@Tag(name = "Recipient", description = "추천 대상")
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
    public ListResponse<RecipientResponse> findAll(@CurrentUser AuthUser authUser) {
        return recipientService.findAll(authUser.userId());
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
