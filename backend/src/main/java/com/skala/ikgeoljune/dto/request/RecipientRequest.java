package com.skala.ikgeoljune.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// SCR-RECIPIENT-001 · UC2
@Getter
@Setter
public class RecipientRequest {

    @NotBlank
    private String name;

    private String relationship;
    private Integer age;
    private String gender;
    private LocalDate upcomingAnniversary;
    private String excludeTags;
}
