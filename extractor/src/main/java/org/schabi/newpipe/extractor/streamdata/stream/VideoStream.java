package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a video (only) stream.
 */
public interface VideoStream extends Stream {
    String UNKNOWN_RESOLUTION = "";

    // TODO: Check if this can be non-null
    @Nullable
    default VideoAudioMediaFormat videoMediaFormat() {
        return null;
    }

    // TODO: This should be a separate entity (containing e.g. height x width + fps)
    @Nonnull
    default String resolution() {
        return UNKNOWN_RESOLUTION;
    }

    @Override
    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof VideoStream)) {
            return false;
        }

        final VideoStream otherVideoStream = (VideoStream) other;
        return Objects.equals(videoMediaFormat(), otherVideoStream.videoMediaFormat())
                && Objects.equals(resolution(), otherVideoStream.resolution());
    }
}
