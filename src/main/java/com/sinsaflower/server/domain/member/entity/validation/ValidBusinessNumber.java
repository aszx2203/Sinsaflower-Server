package com.sinsaflower.server.domain.member.entity.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BusinessNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBusinessNumber {
    String message() default "유효하지 않은 사업자등록번호입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// 사업자등록번호 검증 로직

class BusinessNumberValidator implements ConstraintValidator<ValidBusinessNumber, String> {

    @Override
    public void initialize(ValidBusinessNumber constraintAnnotation) {
        // 초기화 로직
    }

    @Override
    public boolean isValid(String businessNumber, ConstraintValidatorContext context) {
        if (businessNumber == null || businessNumber.isEmpty()) {
            return false;
        }

        // 사업자등록번호는 10자리 숫자
        if (!businessNumber.matches("\\d{10}")) {
            return false;
        }

        // 체크섬 검증 로직
        int[] weights = {1, 3, 7, 1, 3, 7, 1, 3, 5};
        int sum = 0;
        
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(businessNumber.charAt(i)) * weights[i];
        }
        
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == Character.getNumericValue(businessNumber.charAt(9));
    }
} 