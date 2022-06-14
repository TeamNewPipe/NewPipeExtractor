package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.format.AudioMediaFormat;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents a audio (only) stream.
 */
public interface AudioStream extends Stream<AudioMediaFormat>, BaseAudioStream {

    @Override
    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof AudioStream)) {
            return false;
        }

        final AudioStream otherAudioStream = (AudioStream) other;
        return Objects.equals(mediaFormat(), otherAudioStream.mediaFormat())
                && averageBitrate() == otherAudioStream.averageBitrate();
    }
}
