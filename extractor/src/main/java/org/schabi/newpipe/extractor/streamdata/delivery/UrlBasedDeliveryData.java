package org.schabi.newpipe.extractor.streamdata.delivery;

import javax.annotation.Nonnull;

public interface UrlBasedDeliveryData extends DeliveryData {
    @Nonnull
    String url();
}
