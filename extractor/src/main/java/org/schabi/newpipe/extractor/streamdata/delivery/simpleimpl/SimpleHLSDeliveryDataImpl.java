package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.HLSDeliveryData;

import javax.annotation.Nonnull;

public class SimpleHLSDeliveryDataImpl extends AbstractUrlBasedDeliveryDataImpl
        implements HLSDeliveryData {
    public SimpleHLSDeliveryDataImpl(@Nonnull final String url) {
        super(url);
    }
}
