package com.vinberts.shrinkly.web.errors;

/**
 *
 */
public class UserNotAuthorizedException extends RuntimeException {

    public UserNotAuthorizedException() {
        super();
    }

    public UserNotAuthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserNotAuthorizedException(final String message) {
        super(message);
    }

    public UserNotAuthorizedException(final Throwable cause) {
        super(cause);
    }
}
