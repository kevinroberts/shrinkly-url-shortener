package com.vinberts.shrinkly.security;

import java.util.Optional;

/**
 *
 */
public interface ISecurityUserService {

    void removePasswordResetToken(String token);

    Optional<String> validatePasswordResetToken(long id, String token);

}
