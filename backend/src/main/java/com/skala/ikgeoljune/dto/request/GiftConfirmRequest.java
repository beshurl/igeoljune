package com.skala.ikgeoljune.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// UC13 선물 확정 및 이력 저장
@Getter
@Setter
public class GiftConfirmRequest {

    @NotNull
    private Long candidateId;

    private String occasion;
    private LocalDate confirmedDate;
}
