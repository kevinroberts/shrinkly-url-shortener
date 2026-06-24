package com.vinberts.shrinkly.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 *
 */
public interface ISecurityUserService {

    void removePasswordResetToken(String token);

    Optional<String> validatePasswordResetToken(long id, String token, HttpServletRequest request, HttpServletResponse response);

}
