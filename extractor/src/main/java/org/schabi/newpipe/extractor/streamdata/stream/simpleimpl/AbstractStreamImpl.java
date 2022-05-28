package org.schabi.newpipe.extractor.streamdata.stream.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.stream.Stream;

import java.util.Objects;

import javax.annotation.Nonnull;

public abstract class AbstractStreamImpl implements Stream {
    @Nonnull
    private final DeliveryData deliveryData;

    protected AbstractStreamImpl(@Nonnull final DeliveryData deliveryData) {
        this.deliveryData = Objects.requireNonNull(deliveryData);
    }

    @Nonnull
    @Override
    public DeliveryData deliveryData() {
        return deliveryData;
    }
}
