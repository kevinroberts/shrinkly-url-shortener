package com.vinberts.shrinkly.web.dto;

import com.vinberts.shrinkly.validation.PasswordMatches;
import com.vinberts.shrinkly.validation.ValidEmail;
import com.vinberts.shrinkly.validation.ValidPassword;
import com.vinberts.shrinkly.validation.ValidUsername;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 */
@Data
@PasswordMatches
public class UserDto {

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
