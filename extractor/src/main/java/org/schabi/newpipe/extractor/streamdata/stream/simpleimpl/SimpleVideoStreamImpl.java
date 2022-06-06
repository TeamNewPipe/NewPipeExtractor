package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nonnull;

public class SimpleVideoStreamImpl extends AbstractStreamImpl<VideoAudioMediaFormat>
        implements VideoStream {
    @Nonnull
    private final VideoQualityData videoQualityData;

    public SimpleVideoStreamImpl(
            @Nonnull final VideoAudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        super(mediaFormat, deliveryData);
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    public SimpleVideoStreamImpl(
            @Nonnull final VideoAudioMediaFormat mediaFormat,
            @Nonnull final DeliveryData deliveryData
    ) {
        this(mediaFormat, deliveryData, VideoQualityData.fromUnknown());
    }

    @Nonnull
    @Override
    public VideoQualityData videoQualityData() {
        return videoQualityData;
    }
}
