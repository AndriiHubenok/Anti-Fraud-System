package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResultValidator.class)
public @interface ValidResult {
    String message() default "Invalid transaction result";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
