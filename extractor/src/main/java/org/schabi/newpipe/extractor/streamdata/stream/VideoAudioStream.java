package org.schabi.newpipe.extractor.streamdata.stream;

import javax.annotation.Nullable;

/**
 * Represents a combined video+audio stream.
 */
public interface VideoAudioStream extends VideoStream, AudioStream {
    @Override
    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof VideoAudioStream)) {
            return false;
        }

        return VideoStream.super.equalsStream(other) && AudioStream.super.equalsStream(other);
    }
}
