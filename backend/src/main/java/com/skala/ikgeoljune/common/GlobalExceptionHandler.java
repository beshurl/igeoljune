package com.skala.ikgeoljune.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/** 모든 오류를 API.yml ErrorResponse 형태로 변환한다. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(ErrorResponse.of(code, e.getMessage()));
    }

    /** @Valid 검증 실패 → 400 VALIDATION_ERROR, 실패 필드는 fieldErrors 에 담는다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        // 객체 단위 제약(예: 최소 한 개 필드 필요)은 필드명이 없으므로 별도로 모은다.
        List<ErrorResponse.FieldError> globalErrors = e.getBindingResult().getGlobalErrors().stream()
                .map(ge -> new ErrorResponse.FieldError(ge.getObjectName(), ge.getDefaultMessage()))
                .toList();

        List<ErrorResponse.FieldError> all = new java.util.ArrayList<>(fieldErrors);
        all.addAll(globalErrors);
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage(), all));
    }

    /** 쿼리 파라미터 등 메서드 파라미터 제약 위반 → 400 */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(jakarta.validation.ConstraintViolationException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new ErrorResponse.FieldError(field, v.getMessage());
                })
                .toList();
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage(), fieldErrors));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getMessage()));
    }

    /** 413 업로드 파일 허용 크기 초과 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(ErrorCode.PAYLOAD_TOO_LARGE.getStatus())
                .body(ErrorResponse.of(ErrorCode.PAYLOAD_TOO_LARGE, ErrorCode.PAYLOAD_TOO_LARGE.getMessage()));
    }

    /**
     * DB 제약 위반 → 409 RESOURCE_CONFLICT.
     * 계약상 코드는 하나이므로, 어떤 제약을 위반했는지는 message 로 구분한다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        String message = resolveConstraintMessage(e);
        log.warn("데이터 무결성 위반 - {}", message, e);
        return ResponseEntity.status(ErrorCode.RESOURCE_CONFLICT.getStatus())
                .body(ErrorResponse.of(ErrorCode.RESOURCE_CONFLICT, message));
    }

    private String resolveConstraintMessage(DataIntegrityViolationException e) {
        String constraintName = null;
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                constraintName = violation.getConstraintName();
                break;
            }
        }
        if (constraintName == null) {
            return ErrorCode.RESOURCE_CONFLICT.getMessage();
        }
        return switch (constraintName.toLowerCase()) {
            case "users_email_key", "uq_users_email" -> "이미 사용 중인 이메일입니다.";
            case "uq_recipient_preference" -> "이미 등록된 취향입니다.";
            case "feedback_candidate_id_key", "uq_feedback_candidate" -> "이미 등록된 피드백입니다.";
            case "uq_candidate_recommend_rank" -> "같은 추천 안에 중복된 순위가 있습니다.";
            default -> ErrorCode.RESOURCE_CONFLICT.getMessage();
        };
    }

    /** 존재하지 않는 경로 → 404 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    /** 경로는 있으나 메서드가 다른 경우 → 405 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 - {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
