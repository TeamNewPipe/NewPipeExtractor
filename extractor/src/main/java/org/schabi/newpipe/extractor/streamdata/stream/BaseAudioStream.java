package org.schabi.newpipe.extractor.streamdata.stream;

import javax.annotation.Nullable;

public interface BaseAudioStream {
    int UNKNOWN_AVG_BITRATE = -1;

    /**
     * Average audio bitrate in KBit/s.
     *
     * @return the average bitrate or <code>-1</code> if unknown
     */
    default int averageBitrate() {
        return UNKNOWN_AVG_BITRATE;
    }

    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof BaseAudioStream)) {
            return false;
        }

        final BaseAudioStream otherAudioStream = (BaseAudioStream) other;
        return averageBitrate() == otherAudioStream.averageBitrate();
    }
}
