package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.format.MediaFormat;
import org.schabi.newpipe.extractor.streamdata.stream.Stream;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractStreamImpl<M extends MediaFormat> implements Stream<M> {
    @Nonnull
    private final M mediaFormat;
    @Nonnull
    private final DeliveryData deliveryData;

    protected AbstractStreamImpl(
            @Nonnull final M mediaFormat,
            @Nonnull final DeliveryData deliveryData) {
        this.mediaFormat = Objects.requireNonNull(mediaFormat);
        this.deliveryData = Objects.requireNonNull(deliveryData);
    }

    @Nonnull
    @Override
    public M mediaFormat() {
        return mediaFormat;
    }

    @Nonnull
    @Override
    public DeliveryData deliveryData() {
        return deliveryData;
    }
}
