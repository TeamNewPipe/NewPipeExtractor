package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.ProgressiveHTTPDeliveryData;

import javax.annotation.Nonnull;

public class SimpleProgressiveHTTPDeliveryDataImpl extends AbstractUrlBasedDeliveryDataImpl
        implements ProgressiveHTTPDeliveryData {
    public SimpleProgressiveHTTPDeliveryDataImpl(@Nonnull final String url) {
        super(url);
    }
}
