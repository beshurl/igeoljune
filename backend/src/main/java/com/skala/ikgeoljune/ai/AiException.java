package com.skala.ikgeoljune.ai;

/** AI 처리 실패. 서비스 계층에서 §1.4 422 로 변환한다. */
public class AiException extends RuntimeException {

    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
