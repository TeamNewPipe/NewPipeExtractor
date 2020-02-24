package org.schabi.newpipe.extractor.exceptions;

public class ContentNotAvailableException extends ParsingException {

    private String localizedMessage;

    public ContentNotAvailableException(String message) {
        super(message);
    }

    public ContentNotAvailableException(String message, String localizedMessage) {
        super(message);
        this.localizedMessage = localizedMessage;
    }

    public ContentNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentNotAvailableException(String message, String localizedMessage, Throwable cause) {
        super(message, cause);
        this.localizedMessage = localizedMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage != null ? localizedMessage : super.getLocalizedMessage();
    }
}
