package com.vinberts.shrinkly.web.dto;

import com.vinberts.shrinkly.validation.ValidPassword;
import lombok.Data;

/**
 *
 */
@Data
public class PasswordDto {

    private String oldPassword;

    @ValidPassword
    private String newPassword;

}
