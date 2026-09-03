package com.skala.ikgeoljune.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 미전송(null)은 "수정하지 않음" 이므로 통과시킨다.
        return value == null || !value.isBlank();
    }
}
