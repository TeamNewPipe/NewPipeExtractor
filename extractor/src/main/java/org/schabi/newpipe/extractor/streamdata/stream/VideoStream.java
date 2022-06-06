package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents a video (only) stream.
 */
public interface VideoStream extends Stream<VideoAudioMediaFormat> {
    VideoQualityData videoQualityData();

    @Override
    default boolean equalsStream(@Nullable final Stream other) {
        if (!(other instanceof VideoStream)) {
            return false;
        }

        final VideoStream otherVideoStream = (VideoStream) other;
        return Objects.equals(mediaFormat(), otherVideoStream.mediaFormat())
                && videoQualityData().equalsVideoQualityData(otherVideoStream.videoQualityData());
    }
}
