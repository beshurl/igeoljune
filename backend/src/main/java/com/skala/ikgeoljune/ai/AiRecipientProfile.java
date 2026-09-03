package com.skala.ikgeoljune.ai;

/** AI 에 전달하는 recipient 기본 정보 (§8) */
public record AiRecipientProfile(
        String name,
        String relationship,
        String ageGroup,
        String gender,
        String job
) {
}
