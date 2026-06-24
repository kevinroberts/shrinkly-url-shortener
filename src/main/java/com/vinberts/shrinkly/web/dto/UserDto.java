package com.vinberts.shrinkly.web.dto;

import com.vinberts.shrinkly.validation.PasswordMatchable;
import com.vinberts.shrinkly.validation.PasswordMatches;
import com.vinberts.shrinkly.validation.ValidEmail;
import com.vinberts.shrinkly.validation.ValidPassword;
import com.vinberts.shrinkly.validation.ValidUsername;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
@Data
@PasswordMatches
public class UserDto implements PasswordMatchable {

    @NotNull
    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @NotNull
    @Size(min = 1)
    private String matchingPassword;

    @ValidEmail
    @NotNull
    @Size(min = 1, message = "{Size.userDto.email}")
    private String email;

}
