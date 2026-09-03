package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.domain.PreferenceType;
import com.skala.ikgeoljune.domain.SourceType;
import com.skala.ikgeoljune.dto.preference.*;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §5 구조화 취향 API */
@Tag(name = "Preference", description = "구조화 취향")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @Operation(summary = "PREF-001 취향 등록 (sourceType 은 서버가 DIRECT 로 설정)")
    @PostMapping("/recipients/{recipientId}/preferences")
    @ResponseStatus(HttpStatus.CREATED)
    public PreferenceResponse create(@CurrentUser AuthUser authUser,
                                     @PathVariable Long recipientId,
                                     @Valid @RequestBody PreferenceCreateRequest request) {
        return preferenceService.create(recipientId, authUser.userId(), request);
    }

    @Operation(summary = "PREF-002 추출 취향 일괄 저장")
    @PostMapping("/recipients/{recipientId}/preferences/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public ListResponse<PreferenceResponse> createBulk(@CurrentUser AuthUser authUser,
                                                       @PathVariable Long recipientId,
                                                       @Valid @RequestBody PreferenceBulkCreateRequest request) {
        return preferenceService.createBulk(recipientId, authUser.userId(), request);
    }

    @Operation(summary = "PREF-003 취향 목록 조회")
    @GetMapping("/recipients/{recipientId}/preferences")
    public ListResponse<PreferenceResponse> findAll(@CurrentUser AuthUser authUser,
                                                    @PathVariable Long recipientId,
                                                    @RequestParam(required = false) PreferenceType preferenceType,
                                                    @RequestParam(required = false) SourceType sourceType) {
        return preferenceService.findAll(recipientId, authUser.userId(), preferenceType, sourceType);
    }

    @Operation(summary = "PREF-004 취향 수정")
    @PatchMapping("/preferences/{preferenceId}")
    public PreferenceResponse update(@CurrentUser AuthUser authUser,
                                     @PathVariable Long preferenceId,
                                     @Valid @RequestBody PreferenceUpdateRequest request) {
        return preferenceService.update(preferenceId, authUser.userId(), request);
    }

    @Operation(summary = "PREF-005 취향 삭제")
    @DeleteMapping("/preferences/{preferenceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthUser authUser, @PathVariable Long preferenceId) {
        preferenceService.delete(preferenceId, authUser.userId());
    }
}
