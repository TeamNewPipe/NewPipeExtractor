package org.schabi.newpipe.extractor.services.youtube.itag.format;

import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import javax.annotation.Nonnull;

public interface VideoItagFormat extends ItagFormat<VideoAudioMediaFormat> {
    @Nonnull
    VideoQualityData videoQualityData();
}
