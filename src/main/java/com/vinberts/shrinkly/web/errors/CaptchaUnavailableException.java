package com.vinberts.shrinkly.web.errors;

/**
 *
 */
public class CaptchaUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public CaptchaUnavailableException() {
        super();
    }

    public CaptchaUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CaptchaUnavailableException(final String message) {
        super(message);
    }

    public CaptchaUnavailableException(final Throwable cause) {
        super(cause);
    }

}
