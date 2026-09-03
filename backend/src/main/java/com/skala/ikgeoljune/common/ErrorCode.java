package com.skala.ikgeoljune.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API.yml components/responses 가 정의한 오류 코드 체계.
 *
 * <p>프론트엔드는 이 {@code code} 값으로 분기하므로 임의로 세분화하지 않는다.
 * 구체적인 상황 설명은 {@code message} 로 전달한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /** 400 입력값 검증 실패 */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),

    /** 401 인증 토큰 누락·만료 또는 로그인 실패 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    /** 403 다른 사용자의 리소스 접근 */
    RESOURCE_FORBIDDEN(HttpStatus.FORBIDDEN, "접근할 수 없는 리소스입니다."),

    /** 404 리소스를 찾을 수 없음 */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    /** 405 (계약에 없지만 404 로 뭉뚱그리지 않기 위해 분리) */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),

    /** 409 이메일·중복 데이터·리소스 상태 충돌 */
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "현재 상태에서는 요청을 처리할 수 없습니다."),

    /** 413 업로드 파일 허용 크기 초과 */
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "허용 크기를 초과한 파일입니다."),

    /** 415 지원하지 않는 파일 형식 */
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다."),

    /** 422 AI 분석 또는 추천 결과 처리 불가 */
    AI_RESULT_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "AI 결과를 구조화하지 못했습니다."),

    /** 500 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
