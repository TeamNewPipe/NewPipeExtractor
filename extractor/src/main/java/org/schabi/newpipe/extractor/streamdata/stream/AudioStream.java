package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents a audio (only) stream.
 */
public interface AudioStream extends Stream {
    int UNKNOWN_BITRATE = -1;

    // TODO: Check if this can be non-null
    @Nullable
    default AudioMediaFormat audioMediaFormat() {
        return null;
    }

    /**
     * Get the average bitrate of the stream.
     *
     * @return the average bitrate or <code>-1</code> if unknown
     */
    default int averageBitrate() {
        return UNKNOWN_BITRATE;
    }

    @Override
    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof AudioStream)) {
            return false;
        }

        final AudioStream otherAudioStream = (AudioStream) other;
        return Objects.equals(audioMediaFormat(), otherAudioStream.audioMediaFormat())
                && averageBitrate() == otherAudioStream.averageBitrate();
    }
}
