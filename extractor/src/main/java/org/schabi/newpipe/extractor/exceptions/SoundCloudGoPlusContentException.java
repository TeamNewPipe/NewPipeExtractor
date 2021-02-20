package org.schabi.newpipe.extractor.exceptions;

public class SoundCloudGoPlusContentException extends ContentNotAvailableException {
    public SoundCloudGoPlusContentException() {
        super("This track is a SoundCloud Go+ track");
    }

    public SoundCloudGoPlusContentException(Throwable cause) {
        super("This track is a SoundCloud Go+ track", cause);
    }
}
