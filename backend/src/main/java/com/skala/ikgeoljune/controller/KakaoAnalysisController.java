package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.common.ListResponse;
import com.skala.ikgeoljune.dto.preference.ExtractedPreferenceResponse;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.KakaoAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * KAKAO-001 카카오톡 파일 임시 분석.
 * 분석 결과는 응답으로만 반환하며 원문 파일은 저장하지 않는다.
 */
@Tag(name = "KakaoAnalysis", description = "카카오톡 대화 분석")
@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class KakaoAnalysisController {

    private final KakaoAnalysisService kakaoAnalysisService;

    @Operation(summary = "KAKAO-001 카카오톡 파일 임시 분석")
    @PostMapping(value = "/{recipientId}/kakao-analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ListResponse<ExtractedPreferenceResponse> analyze(@CurrentUser AuthUser authUser,
                                                             @PathVariable Long recipientId,
                                                             @RequestPart("file") MultipartFile file) {
        return kakaoAnalysisService.analyze(recipientId, authUser.userId(), file);
    }
}
