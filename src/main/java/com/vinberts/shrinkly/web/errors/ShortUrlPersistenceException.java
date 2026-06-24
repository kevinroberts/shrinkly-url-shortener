package com.vinberts.shrinkly.web.errors;

/**
 * Thrown when a short code could not be persisted to its backing store (Redis).
 * Signals that the link does not actually resolve, so callers must not report
 * success or write a corresponding Postgres row.
 */
public class ShortUrlPersistenceException extends RuntimeException {

    private static final long serialVersionUID = 7129809541236748921L;

    public ShortUrlPersistenceException() {
        super();
    }

    public ShortUrlPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ShortUrlPersistenceException(final String message) {
        super(message);
    }

    public ShortUrlPersistenceException(final Throwable cause) {
        super(cause);
    }

}
