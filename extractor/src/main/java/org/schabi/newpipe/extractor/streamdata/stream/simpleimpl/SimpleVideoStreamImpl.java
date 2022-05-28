package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.VideoAudioMediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.VideoStream;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleVideoStreamImpl extends AbstractStreamImpl implements VideoStream {
    @Nullable
    private final VideoAudioMediaFormat videoAudioMediaFormat;
    @Nonnull
    private final String resolution;

    public SimpleVideoStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nullable final VideoAudioMediaFormat videoAudioMediaFormat,
            @Nonnull final String resolution
    ) {
        super(deliveryData);
        this.videoAudioMediaFormat = videoAudioMediaFormat;
        this.resolution = Objects.requireNonNull(resolution);
    }

    public SimpleVideoStreamImpl(
            @Nonnull final DeliveryData deliveryData,
            @Nonnull final String resolution
    ) {
        this(deliveryData, null, resolution);
    }

    public SimpleVideoStreamImpl(@Nonnull final DeliveryData deliveryData) {
        this(deliveryData, null, UNKNOWN_RESOLUTION);
    }

    @Nullable
    @Override
    public VideoAudioMediaFormat videoMediaFormat() {
        return videoAudioMediaFormat;
    }

    @Nonnull
    @Override
    public String resolution() {
        return resolution;
    }
}
