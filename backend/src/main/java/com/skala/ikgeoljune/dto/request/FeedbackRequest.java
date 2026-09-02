package com.skala.ikgeoljune.dto.request;

import lombok.Getter;
import lombok.Setter;

// SCR-AI-002 · UC10·UC11
@Getter
@Setter
public class FeedbackRequest {
    private Long candidateId;
    private boolean liked;
    private String dislikeReason;
}
