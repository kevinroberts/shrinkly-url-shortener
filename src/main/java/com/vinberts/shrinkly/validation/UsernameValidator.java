package com.vinberts.shrinkly.validation;

import org.apache.commons.validator.routines.RegexValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 */
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    @Override
    public void initialize(final ValidUsername constraintAnnotation) {
        //
    }

    @Override
    public boolean isValid(final String username, final ConstraintValidatorContext context) {
        // modified example of https://stackoverflow.com/questions/12018245/regular-expression-to-validate-username
        RegexValidator validator = new RegexValidator("^(?=.{3,30}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$");

        return validator.isValid(username);
    }

}
