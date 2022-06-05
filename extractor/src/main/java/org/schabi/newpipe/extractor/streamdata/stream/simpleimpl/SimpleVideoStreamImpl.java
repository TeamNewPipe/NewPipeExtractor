package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleVideoStreamImpl extends AbstractStreamImpl implements VideoStream {
    @Nullable
    private final VideoAudioMediaFormat videoAudioMediaFormat;
    @Nonnull
    private final VideoQualityData videoQualityData;

    public SimpleVideoStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        super(deliveryData);
        this.videoAudioMediaFormat = videoAudioMediaFormat;
        this.videoQualityData = Objects.requireNonNull(videoQualityData);
    }

    public SimpleVideoStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nonnull final VideoQualityData videoQualityData
    ) {
        this(deliveryData, null, videoQualityData);
    }

    public SimpleVideoStreamImpl(@Nonnull final DeliveryData deliveryData) {
        this(deliveryData, VideoQualityData.fromUnknown());
    }

    @Nullable
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
