package com.vinberts.shrinkly.web.errors;

/**
 * Thrown when an invitation cannot be created or accepted (existing account,
 * already-pending invitation, or an invalid/expired token).
 */
public class InvitationException extends RuntimeException {

    private static final long serialVersionUID = 4471812345987612345L;

    public InvitationException(final String message) {
        super(message);
    }
}
