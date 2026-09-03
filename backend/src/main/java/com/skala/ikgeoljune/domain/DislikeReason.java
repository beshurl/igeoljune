package com.skala.ikgeoljune.domain;

/** API 명세서 §10 dislikeReason */
public enum DislikeReason {
    /** 취향과 맞지 않음 */
    TASTE_MISMATCH,
    /** 이미 가지고 있음 */
    ALREADY_OWNED,
    /** 과거 선물과 유사함 */
    SIMILAR_TO_PREVIOUS,
    /** 관계에 비해 부담스러움 */
    TOO_BURDENSOME,
    /** 실용성이 부족함 */
    NOT_PRACTICAL,
    /** 가격이 적절하지 않음 */
    PRICE_INAPPROPRIATE,
    /** 다른 스타일을 원함 */
    WANT_DIFFERENT_STYLE,
    /** 기타 */
    OTHER;

    public String description() {
        return switch (this) {
            case TASTE_MISMATCH -> "취향과 맞지 않음";
            case ALREADY_OWNED -> "이미 가지고 있음";
            case SIMILAR_TO_PREVIOUS -> "과거 선물과 유사함";
            case TOO_BURDENSOME -> "관계에 비해 부담스러움";
            case NOT_PRACTICAL -> "실용성이 부족함";
            case PRICE_INAPPROPRIATE -> "가격이 적절하지 않음";
            case WANT_DIFFERENT_STYLE -> "다른 스타일을 원함";
            case OTHER -> "기타";
        };
    }
}
