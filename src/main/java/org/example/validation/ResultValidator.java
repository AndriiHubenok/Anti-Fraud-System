package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ResultValidator implements ConstraintValidator<ValidResult, String> {
    private final List<String> allowedResults = List.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && allowedResults.contains(value);
    }
}
