package com.vinberts.shrinkly.validation;

/**
 * Implemented by DTOs validated with {@link PasswordMatches} so the validator
 * is not coupled to a single DTO type.
 */
public interface PasswordMatchable {
    String getPassword();
    String getMatchingPassword();
}
