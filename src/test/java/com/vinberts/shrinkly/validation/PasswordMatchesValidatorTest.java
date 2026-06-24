package com.vinberts.shrinkly.validation;

import com.vinberts.shrinkly.web.dto.InvitationAcceptDto;
import com.vinberts.shrinkly.web.dto.UserDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordMatchesValidatorTest {

    private final PasswordMatchesValidator validator = new PasswordMatchesValidator();

    @Test
    void matchingInvitationAcceptDtoIsValid() {
        InvitationAcceptDto dto = new InvitationAcceptDto();
        dto.setPassword("Secret123!");
        dto.setMatchingPassword("Secret123!");
        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void mismatchedInvitationAcceptDtoIsInvalid() {
        InvitationAcceptDto dto = new InvitationAcceptDto();
        dto.setPassword("Secret123!");
        dto.setMatchingPassword("different");
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    void matchingUserDtoIsValid() {
        UserDto dto = new UserDto();
        dto.setPassword("Secret123!");
        dto.setMatchingPassword("Secret123!");
        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void nullPasswordIsInvalid() {
        InvitationAcceptDto dto = new InvitationAcceptDto();
        assertFalse(validator.isValid(dto, null));
    }
}
