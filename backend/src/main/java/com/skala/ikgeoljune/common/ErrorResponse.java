package com.skala.ikgeoljune.common;

import java.util.List;

/** API 명세서 §1.4 오류 응답 포맷 */
public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String reason) {
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(errorCode.name(), message, fieldErrors);
    }
}
