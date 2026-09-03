package com.skala.ikgeoljune.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * PATCH 요청 전용 제약.
 *
 * <p>필드를 보내지 않은 경우(null)는 허용하지만, 보냈다면 공백만으로 이루어질 수 없다.
 * 생성 요청의 {@code @NotBlank} 불변식이 수정 API 로 우회되는 것을 막는다.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
public @interface NotBlankIfPresent {

    String message() default "값을 보낼 경우 공백일 수 없습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
