package com.vinberts.shrinkly.web.dto;

import com.vinberts.shrinkly.validation.PasswordMatchable;
import com.vinberts.shrinkly.validation.PasswordMatches;
import com.vinberts.shrinkly.validation.ValidPassword;
import com.vinberts.shrinkly.validation.ValidUsername;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Backs the invitation-accept form. The email is NOT a field here — it is taken
 * from the invitation looked up by token.
 */
@Data
@PasswordMatches
public class InvitationAcceptDto implements PasswordMatchable {

    @NotNull
    private String token;

    @NotNull
    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @NotNull
    @Size(min = 1)
    private String matchingPassword;
}
