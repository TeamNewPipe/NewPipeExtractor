package org.schabi.newpipe.extractor.exceptions;

public class SoundCloudGoPlusException extends ContentNotAvailableException {
    public SoundCloudGoPlusException() {
        super("This track is a SoundCloud Go+ track");
    }

    public SoundCloudGoPlusException(Throwable cause) {
        super("This track is a SoundCloud Go+ track", cause);
    }
}