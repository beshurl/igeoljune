package com.skala.ikgeoljune.common;

import java.util.List;

/** API.yml *ListResponse 공통 형태 */
public record ListResponse<T>(
        List<T> items,
        long totalCount
) {
    public static <T> ListResponse<T> of(List<T> items) {
        return new ListResponse<>(items, items.size());
    }

    /** 페이지네이션 응답: totalCount 는 현재 페이지가 아니라 전체 건수다. */
    public static <T> ListResponse<T> of(List<T> items, long totalCount) {
        return new ListResponse<>(items, totalCount);
    }
}
