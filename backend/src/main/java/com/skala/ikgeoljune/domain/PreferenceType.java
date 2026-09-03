package com.skala.ikgeoljune.domain;

/** API 명세서 §10 preferenceType */
public enum PreferenceType {
    /** 관심사·취미 */
    INTEREST,
    /** 선호 상품 카테고리 */
    PREFERRED_CATEGORY,
    /** 선호 속성·스타일 */
    PREFERRED_ATTRIBUTE,
    /** 비선호 또는 제외 카테고리 */
    DISLIKED_CATEGORY,
    /** 필요하거나 갖고 싶은 품목 */
    WISH_ITEM
}
