package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import javax.annotation.Nonnull;

/**
 * Represents a video (only) stream.
 */
public interface VideoStream extends Stream<VideoAudioMediaFormat> {
    @Nonnull
    VideoQualityData qualityData();
}
