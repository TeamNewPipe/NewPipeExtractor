package org.schabi.newpipe.extractor.exceptions;

/**
 * Content can't be extracted because the service requires logging in to confirm the user is not a
 * bot. Can usually only be solvable by changing IP (e.g. in the case of YouTube).
 */
public class SignInConfirmNotBotException extends ParsingException {
    public SignInConfirmNotBotException(final String message) {
        super(message);
    }
}
