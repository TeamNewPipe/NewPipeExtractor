package org.schabi.newpipe.extractor.streamdata.stream;

import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Stream {
    @Nonnull
    DeliveryData deliveryData();


    // TODO: May also have to check deliverydata
    boolean equalsStream(@Nullable final Stream other);
}
