package org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl;

import org.schabi.newpipe.extractor.streamdata.delivery.DASHUrlDeliveryData;

import javax.annotation.Nonnull;

public class SimpleDASHUrlDeliveryDataImpl extends AbstractUrlBasedDeliveryDataImpl
        implements DASHUrlDeliveryData {
    public SimpleDASHUrlDeliveryDataImpl(@Nonnull final String url) {
        super(url);
    }
}
