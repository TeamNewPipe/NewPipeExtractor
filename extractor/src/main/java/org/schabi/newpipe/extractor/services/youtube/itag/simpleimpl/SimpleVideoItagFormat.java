package org.schabi.newpipe.extractor.services.youtube.itag.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.VideoItagFormat;
import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SimpleVideoItagFormat extends AbstractItagFormat implements VideoItagFormat {
    @Nonnull
    private final VideoAudioMediaFormat videoAudioMediaFormat;
    @Nonnull
    private final VideoQualityData videoQualityData;

    public SimpleVideoItagFormat(final int id,
                                 @Nonnull final VideoAudioMediaFormat videoAudioMediaFormat,
                                 @Nonnull final VideoQualityData videoQualityData,
                                 @Nonnull final ItagFormatDeliveryData deliveryData) {
        super(id, deliveryData);
        this.videoAudioMediaFormat = Objects.requireNonNull(videoAudioMediaFormat);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    public SimpleVideoItagFormat(final int id,
                                 @Nonnull final VideoAudioMediaFormat videoAudioMediaFormat,
                                 @Nonnull final VideoQualityData videoQualityData) {
        super(id);
        this.videoAudioMediaFormat = Objects.requireNonNull(videoAudioMediaFormat);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    @Nonnull
    @Override
    public VideoAudioMediaFormat videoMediaFormat() {
        return videoAudioMediaFormat;
    }

    @Nonnull
    @Override
    public VideoQualityData videoQualityData() {
        return videoQualityData;
    }
}
