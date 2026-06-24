package com.vinberts.shrinkly.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 *
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        if (!(obj instanceof PasswordMatchable matchable)) {
            return false;
        }
        final String password = matchable.getPassword();
        return password != null && password.equals(matchable.getMatchingPassword());
    }

}
