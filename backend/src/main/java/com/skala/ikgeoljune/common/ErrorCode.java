package com.skala.ikgeoljune.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API 명세서 §1.4 오류 응답의 code 값 정의.
 * 프론트엔드는 HTTP status 가 아니라 이 code 로 분기한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INVALID_BUDGET_RANGE(HttpStatus.BAD_REQUEST, "최소 예산은 최대 예산보다 클 수 없습니다."),
    INVALID_FEEDBACK_TYPE(HttpStatus.BAD_REQUEST, "피드백 유형이 올바르지 않습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 토큰이 없거나 만료되었습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 403
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    RECIPIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 대상을 찾을 수 없습니다."),
    PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "취향 정보를 찾을 수 없습니다."),
    PREVIOUS_GIFT_NOT_FOUND(HttpStatus.NOT_FOUND, "과거 선물을 찾을 수 없습니다."),
    GIFT_CONDITION_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 조건을 찾을 수 없습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 결과를 찾을 수 없습니다."),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 후보를 찾을 수 없습니다."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 피드백이 없습니다."),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // 409
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    PREFERENCE_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 취향입니다."),
    GIFT_CONDITION_HAS_RECOMMENDATIONS(HttpStatus.CONFLICT, "추천 결과가 있는 조건은 삭제할 수 없습니다."),

    // 413 / 415
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "허용 크기를 초과한 파일입니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다."),

    // 422
    KAKAO_ANALYSIS_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "카카오톡 대화 분석에 실패했습니다."),
    AI_RECOMMENDATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "AI 추천 결과를 처리할 수 없습니다."),
    RECOMMENDATION_NOT_COMPLETED(HttpStatus.UNPROCESSABLE_ENTITY, "아직 완료되지 않은 추천입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
