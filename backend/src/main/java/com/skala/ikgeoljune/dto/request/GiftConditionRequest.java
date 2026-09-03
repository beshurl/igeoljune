package com.skala.ikgeoljune.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

// SCR-GIFT-001 · UC7 (대표 흐름) — 예산 최우선 원칙: budget은 필수
@Getter
@Setter
public class GiftConditionRequest {

    @NotNull
    private Integer budget;

    private LocalDate anniversaryDate;
    private List<String> preferenceTags;
    private List<String> excludeTags;
}
