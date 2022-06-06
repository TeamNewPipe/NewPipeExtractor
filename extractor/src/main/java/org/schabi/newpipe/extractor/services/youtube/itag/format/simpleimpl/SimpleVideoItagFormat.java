package org.schabi.newpipe.extractor.services.youtube.itag.format.simpleimpl;

import org.schabi.newpipe.extractor.services.youtube.itag.delivery.ItagFormatDeliveryData;
import org.schabi.newpipe.extractor.services.youtube.itag.format.VideoItagFormat;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SimpleVideoItagFormat extends AbstractSimpleItagFormat<VideoAudioMediaFormat>
        implements VideoItagFormat {
    @Nonnull
    private final VideoQualityData videoQualityData;

    public SimpleVideoItagFormat(final int id,
                                 @Nonnull final VideoAudioMediaFormat mediaFormat,
                                 @Nonnull final VideoQualityData videoQualityData,
                                 @Nonnull final ItagFormatDeliveryData deliveryData) {
        super(id, mediaFormat, deliveryData);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    public SimpleVideoItagFormat(final int id,
                                 @Nonnull final VideoAudioMediaFormat mediaFormat,
                                 @Nonnull final VideoQualityData videoQualityData) {
        super(id, mediaFormat);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    @Nonnull
    @Override
    public VideoQualityData videoQualityData() {
        return videoQualityData;
    }
}
