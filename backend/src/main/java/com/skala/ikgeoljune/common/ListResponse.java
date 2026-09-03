package com.skala.ikgeoljune.common;

import java.util.List;

/** API 명세서 §1.3 목록 조회 공통 응답 */
public record ListResponse<T>(
        List<T> items,
        int totalCount
) {
    public static <T> ListResponse<T> of(List<T> items) {
        return new ListResponse<>(items, items.size());
    }
}
