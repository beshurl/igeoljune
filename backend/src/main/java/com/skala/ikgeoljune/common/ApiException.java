package com.skala.ikgeoljune.common;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** 코드는 계약값을 유지하고 상황 설명만 구체화한다. */
    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static ApiException notFound(String message) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(ErrorCode.RESOURCE_CONFLICT, message);
    }

    public static ApiException validation(String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message);
    }
}
