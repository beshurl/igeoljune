package com.skala.ikgeoljune.controller;

import com.skala.ikgeoljune.dto.feedback.FeedbackResponse;
import com.skala.ikgeoljune.dto.feedback.FeedbackUpsertRequest;
import com.skala.ikgeoljune.security.AuthUser;
import com.skala.ikgeoljune.security.CurrentUser;
import com.skala.ikgeoljune.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** §9 피드백 API */
@Tag(name = "Feedback", description = "추천 후보 피드백")
@RestController
@RequestMapping("/api/v1/recommendation-candidates")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "FEEDBACK-001 피드백 등록·변경 (upsert)")
    @PutMapping("/{candidateId}/feedback")
    public FeedbackResponse upsert(@CurrentUser AuthUser authUser,
                                   @PathVariable Long candidateId,
                                   @Valid @RequestBody FeedbackUpsertRequest request) {
        return feedbackService.upsert(candidateId, authUser.userId(), request);
    }

    @Operation(summary = "FEEDBACK-002 피드백 조회 (없으면 404)")
    @GetMapping("/{candidateId}/feedback")
    public FeedbackResponse findOne(@CurrentUser AuthUser authUser, @PathVariable Long candidateId) {
        return feedbackService.findOne(candidateId, authUser.userId());
    }

    @Operation(summary = "FEEDBACK-003 피드백 취소")
    @DeleteMapping("/{candidateId}/feedback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@CurrentUser AuthUser authUser, @PathVariable Long candidateId) {
        feedbackService.delete(candidateId, authUser.userId());
    }
}
