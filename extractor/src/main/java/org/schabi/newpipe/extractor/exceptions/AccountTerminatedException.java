package org.schabi.newpipe.extractor.exceptions;

public class AccountTerminatedException extends ContentNotAvailableException {

    private Reason reason = Reason.UNKNOWN;

    public AccountTerminatedException(final String message) {
        super(message);
    }

    public AccountTerminatedException(final String message, final Reason reason) {
        super(message);
        this.reason = reason;
    }

    public AccountTerminatedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * The reason for the violation. There should also be more info in the exception's message.
     */
    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        UNKNOWN,
        VIOLATION
    }
}
