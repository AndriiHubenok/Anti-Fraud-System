package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class RegionValidator implements ConstraintValidator<ValidRegion, String> {
    private final List<String> allowedRegions = List.of("EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && allowedRegions.contains(value);
    }
}
