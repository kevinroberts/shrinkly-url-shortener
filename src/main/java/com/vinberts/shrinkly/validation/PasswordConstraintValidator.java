package com.vinberts.shrinkly.validation;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(final ValidPassword arg0) {

    }

    @Override
    public boolean isValid(final String password, final ConstraintValidatorContext context) {
        // @formatter:off
        final PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                // at least one lower-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),
                //new SpecialCharacterRule(1),
                new WhitespaceRule()));
        final RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        List<String> errors = validator.getMessages(result);
        List<String> translatedErrors = Lists.newArrayList();
        for (String error: errors) {
            // http://www.passay.org/reference/ for errors
            if (error.startsWith("INSUFFICIENT_UPPERCASE")) {
                translatedErrors.add("Your password must contain at least 1 uppercase character.");
            }
            if (error.startsWith("INSUFFICIENT_DIGIT")) {
                translatedErrors.add("Your password must contain at least 1 numerical character. [1-9]");
            }
            if (error.startsWith("TOO_SHORT")) {
                translatedErrors.add("Your password must be at least 8 characters in length.");
            }
            if (error.startsWith("TOO_LONG")) {
                translatedErrors.add("Your password must be no more than 30 characters in length.");
            }
            if (error.startsWith("ILLEGAL_WHITESPACE")) {
                translatedErrors.add("Password cannot contain whitespace characters.");
            }
            if (error.startsWith("INSUFFICIENT_ALPHABETICAL")) {
                translatedErrors.add("Password must contain at least 3 alphabetical characters.");
            }
            if (error.startsWith("INSUFFICIENT_SPECIAL")) {
                translatedErrors.add("Password must contain at least 1 special characters. (!, @, #, $, etc.)");
            }
        }

        context.buildConstraintViolationWithTemplate(Joiner.on("<br>").join(translatedErrors)).addConstraintViolation();
        return false;
    }

}
