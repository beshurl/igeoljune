package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.recommendation.RecommendationCandidateResponse;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.CandidateSelectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 최종 선물 선택 API */
@Tag(name = "CandidateSelection", description = "최종 선물 선택")
@RestController
@RequestMapping("/api/v1/recommendation-candidates")
@RequiredArgsConstructor
public class CandidateSelectionController {

    private final CandidateSelectionService candidateSelectionService;

    @Operation(summary = "최종 선물 선택 (추천 실행당 1건)")
    @PutMapping("/{candidateId}/selection")
    public RecommendationCandidateResponse select(@CurrentUser AuthUser authUser,
                                                  @PathVariable Long candidateId) {
        return candidateSelectionService.select(candidateId, authUser.userId());
    }

    @Operation(summary = "선택 상태 조회")
    @GetMapping("/{candidateId}/selection")
    public RecommendationCandidateResponse findOne(@CurrentUser AuthUser authUser,
                                                   @PathVariable Long candidateId) {
        return candidateSelectionService.findOne(candidateId, authUser.userId());
    }

    @Operation(summary = "최종 선물 선택 취소")
    @DeleteMapping("/{candidateId}/selection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deselect(@CurrentUser AuthUser authUser, @PathVariable Long candidateId) {
        candidateSelectionService.deselect(candidateId, authUser.userId());
    }
}
